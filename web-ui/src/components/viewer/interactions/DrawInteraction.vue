<template>
<div>
  <ol-vector-layer>
    <ol-source-vector ref="olSourceDrawTarget">
      <ol-interaction-draw
        v-if="nbActiveLayers > 0 || drawCorrection"
        ref="olDrawInteraction"
        :type="drawType"
        :freehand="drawFreehand"
        :geometry-function="drawGeometryFunction"
        @drawend="drawEndHandler"
      />
    </ol-source-vector>
  </ol-vector-layer>
</div>
</template>

<script>
import eventBus from '@/utils/event-bus';

import { get } from '@/utils/store-helpers';

import Polygon, { fromCircle as polygonFromCircle } from 'ol/geom/Polygon';
import WKT from 'ol/format/WKT';

import { Annotation, AnnotationType, Cytomine } from '@/api';
import { Action } from '@/utils/annotation-utils.js';
import { updateAnnotationLinkProperties } from '@/utils/annotation-utils';

export default {
  name: 'draw-interaction',
  props: {
    index: String
  },
  data() {
    return {
      format: new WKT()
    };
  },
  computed: {
    currentUser: get('currentUser/user'),
    imageModule() {
      return this.$store.getters['currentProject/imageModule'](this.index);
    },
    imageWrapper() {
      return this.$store.getters['currentProject/currentViewer'].images[this.index];
    },
    rotation() {
      return this.imageWrapper.view.rotation;
    },
    termsToAssociate() {
      return this.imageWrapper.draw.termsNewAnnots;
    },
    tracksToAssociate() {
      return this.imageWrapper.draw.tracksNewAnnots;
    },
    image() {
      return this.imageWrapper.imageInstance;
    },
    imageGroupId() {
      return this.$store.getters[this.imageModule + 'imageGroupId'];
    },
    slice() {
      // Cannot draw on multiple slices at same time
      return (this.imageWrapper.activeSlices) ? this.imageWrapper.activeSlices[0] : null;
    },
    activeTool() {
      return this.imageWrapper.draw.activeTool;
    },
    activeEditTool() {
      return this.imageWrapper.draw.activeEditTool;
    },
    selectedFeature() {
      return this.$store.getters[this.imageModule + 'selectedFeature'];
    },
    drawType() {
      switch (this.activeTool) {
        case 'point':
          return 'Point';
        case 'line':
        case 'freehand-line':
          return 'LineString';
        case 'magic-wand':
        case 'rectangle':
        case 'circle':
          return 'Circle';
        case 'polygon':
        case 'freehand-polygon':
        case 'select': // correct mode
          return 'Polygon';
        default:
          return ''; // Should not happen
      }
    },
    drawCorrection() {
      return this.activeTool === 'select';
    },
    drawFreehand() {
      return this.activeTool === 'freehand-polygon' || this.activeTool === 'freehand-line' || this.drawCorrection;
    },
    drawGeometryFunction() {
      if (!['magic-wand', 'rectangle'].includes(this.activeTool)) {
        return;
      }

      return (coordinates, geometry) => {
        let rotatedCoords = this.rotateCoords(coordinates, this.rotation);

        let [firstCorner, thirdCorner] = rotatedCoords;
        let secondCorner = [thirdCorner[0], firstCorner[1]];
        let fourthCorner = [firstCorner[0], thirdCorner[1]];

        let rotatedBoxCoordinates = [firstCorner, secondCorner, thirdCorner, fourthCorner, firstCorner];
        let boxCoordinates = [this.rotateCoords(rotatedBoxCoordinates, -this.rotation)];

        if (geometry) {
          geometry.setCoordinates(boxCoordinates);
        } else {
          geometry = new Polygon(boxCoordinates);
        }
        return geometry;
      };
    },
    layers() {
      return this.imageWrapper.layers.selectedLayers || [];
    },
    activeLayers() {
      return this.layers.filter(layer => layer.drawOn);
    },
    nbActiveLayers() {
      return this.activeLayers.length;
    }
  },

  methods: {
    rotateCoords(coords, theta) {
      let cosTheta = Math.cos(theta);
      let sinTheta = Math.sin(theta);
      return coords.map(([x, y]) => [x * cosTheta + y * sinTheta, -x * sinTheta + y * cosTheta]);
    },

    clearDrawnFeatures() {
      if (this.$refs.olSourceDrawTarget) {
        this.$refs.olSourceDrawTarget.source.clear(true);
      }
    },

    async drawEndHandler({ feature }) {
      if (this.drawCorrection) {
        await this.endCorrection(feature);
      } else if (this.nbActiveLayers > 0) {
        await this.endDraw(feature);
      }

      this.clearDrawnFeatures();
    },

    async endDraw(drawnFeature) {
      this.activeLayers.forEach(async (layer, idx) => {
        let annot = new Annotation({
          location: this.getWktLocation(drawnFeature),
          image: this.image.id,
          slice: this.slice.id,
          user: layer.id,
          term: this.termsToAssociate,
          track: this.tracksToAssociate
        });

        try {
          await annot.save();
          annot.userByTerm = this.termsToAssociate.map(term => ({ term, user: [this.currentUser.id] }));
          annot.imageGroup = this.imageGroupId;
          updateAnnotationLinkProperties(annot);
          eventBus.emit('addAnnotation', annot);
          if (idx === this.nbActiveLayers - 1) {
            eventBus.emit('selectAnnotation', { index: this.index, annot });
          }

          this.$store.commit(this.imageModule + 'addAction', { annot, type: Action.CREATE });
        } catch (err) {
          console.log(err);
          this.$notify({ type: 'error', text: this.$t('notif-error-annotation-creation') });
        }

        if (this.activeTool === 'magic-wand') {
          try {
            const annotationId = annot.id;
            const annotation = (await Cytomine.instance.api.post(`annotations/${annotationId}/refine`)).data;

            eventBus.emit('editAnnotation', annotation);
            eventBus.emit('reloadAnnotationCrop', annotation);
            this.$notify({ type: 'success', text: 'Successful SAM Processing !' });
          } catch (error) {
            console.error(error);
            this.$notify({ type: 'error', text: 'Error in SAM Processing.' });
          }
        }
      });
    },

    async endCorrection(feature) {
      if (!this.selectedFeature) {
        return;
      }

      try {
        let annot = this.selectedFeature.properties.annot;
        let correctedAnnot = await Annotation.correctAnnotations({
          location: this.getWktLocation(feature),
          review: annot.type === AnnotationType.REVIEWED,
          remove: (this.activeEditTool === 'correct-remove'),
          annotation: annot.id
        });
        if (correctedAnnot) {
          correctedAnnot.userByTerm = annot.userByTerm; // copy terms from initial annot
          correctedAnnot.track = annot.track;
          correctedAnnot.annotationTrack = annot.annotationTrack;
          correctedAnnot.group = annot.group;
          correctedAnnot.annotationLink = annot.annotationLink;
          correctedAnnot.imageGroup = annot.imageGroup;
          this.$store.commit(this.imageModule + 'addAction', { annot: correctedAnnot, type: Action.UPDATE });
          eventBus.emit('editAnnotation', correctedAnnot);
          eventBus.emit('reloadAnnotationCrop', annot);
        }
      } catch (err) {
        console.log(err);
        this.$notify({ type: 'error', text: this.$t('notif-error-annotation-correction') });
      }
    },

    getWktLocation(feature) {
      // transform circle to circular polygon
      let geometry = feature.getGeometry();
      if (geometry.getType() === 'Circle') {
        feature.setGeometry(polygonFromCircle(geometry));
      }
      return this.format.writeFeature(feature);
    },
  }
};
</script>
