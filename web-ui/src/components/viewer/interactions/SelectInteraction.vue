<template>
<ol-interaction-select
  :filter="filterFunction"
  :toggle-condition="never"
  :remove-condition="shiftKeyOnly"
  :multi="true"
  ref="olSelect"
/>
</template>

<script>
import { markRaw } from 'vue';

import { isCluster } from '@/utils/style-utils.js';
import { never, shiftKeyOnly } from 'ol/events/condition';

import { createGeoJsonFmt } from '@/viewer-ol/geojson.js';
import { injectViewerContext } from '@/viewer-ol/context.js';

export default {
  name: 'select-interaction',
  inject: injectViewerContext,
  props: {
    index: String
  },
  data() {
    return {
      olSelectObject: null,
      applyingStoreSelection: false,
      format: markRaw(createGeoJsonFmt())
    };
  },
  computed: {
    imageModule() {
      return this.$store.getters['currentProject/imageModule'](this.index);
    },
    imageWrapper() {
      return this.$store.getters['currentProject/currentViewer'].images[this.index];
    },
    selectTargetName() {
      return `select-target-${this.index}`;
    },
    selectedFeatures: {
      get() {
        return this.imageWrapper.selectedFeatures.selectedFeatures;
      },
      set(value) {
        //used when selecting a vertex of a feature
        let notAnnotations = value.filter(x => !Object.keys(x).includes('id') && x.properties === null);
        if (notAnnotations.length === 1) {
          value = notAnnotations;
          this.$store.commit(this.imageModule + 'setSelectedFeatures', value);
          return;
        }

        value.sort(
          function (a, b) {
            if (a.properties.annot.area > b.properties.annot.area) {
              return 1;
            } else {
              return -1;
            }
          }
        );

        let previousTarget = this.imageWrapper.selectedFeatures.selectionTargetedFeatures;
        let previousSelectedFeature = this.imageWrapper.selectedFeatures.selectedFeatures[0];

        this.$store.commit(this.imageModule + 'setSelectionTargetedFeatures', value);

        //see https://github.com/cytomine/Cytomine-Web-UI/issues/13 for more details of this algorithm
        if (this.imageWrapper.selectedFeatures.selectedFeatures.length === 0) {
          if (value.length >= 1) {
            value = [value[0]];
          }
        } else {
          if (value.length > 1) {
            //if previous selection is in the new target, we will take the first element not yet visited
            if (value.map(x => x.id).includes(previousSelectedFeature.id)) {
              let index = previousTarget.findIndex(x => x.id === previousSelectedFeature.id);
              let visitedFeatures = previousTarget.slice(0, index + 1);
              index = 0;
              for (let i = 0; i < value.length; i++) {
                if (!visitedFeatures.map(x => x.id).includes(value[i].id)) {
                  index = i;
                  break;
                }
              }
              value = [value[index]];
            } else {
              value = [value[0]];
            }
          }
        }

        if (value.length >= 1 && value[0].properties !== null) {
          let annot = value[0].properties.annot;
          if (typeof annot.recordAction === 'function') {
            annot.recordAction();
          }
        }
        this.$store.commit(this.imageModule + 'setSelectedFeatures', value);
        this.$store.commit(this.imageModule + 'setShowSimilarAnnotations', false);
      }
    },
    terms() {
      return this.imageWrapper.style.terms || [];
    },
    styleFunction() {
      this.imageWrapper.selectedFeatures.selectedFeatures;
      this.imageWrapper.style.layersOpacity;
      this.terms.forEach(term => {
        term.visible;
        term.color;
        term.opacity;
      });
      this.imageWrapper.style.displayNoTerm;
      this.imageWrapper.style.noTermOpacity;
      this.imageWrapper.draw.activeEditTool; // style is different in edit mode (vertices displayed)
      this.imageWrapper.properties.selectedPropertyValues;
      this.imageWrapper.properties.selectedPropertyColor;
      this.imageWrapper.review.reviewMode;
      if (this.imageWrapper.style.wrappedTracks) {
        this.imageWrapper.style.wrappedTracks.forEach(track => {
          track.color;
        });
      }

      let genStyleFunction = this.$store.getters[this.imageModule + 'genStyleFunction'];
      return (feature, resolution) => genStyleFunction(feature, resolution);
    },
    filterFunction() {
      return feature => !isCluster(feature);
    },
    never() {
      return never;
    },
    shiftKeyOnly() {
      return shiftKeyOnly;
    }
  },
  watch: {
    styleFunction(styleFunction) {
      this.applySelectStyle(styleFunction);
    },
    selectedFeatures(value) {
      this.applyStoreSelection(value);
    }
  },
  methods: {
    applySelectStyle(styleFunction) {
      if (!this.olSelectObject) {
        return;
      }
      this.olSelectObject.style_ = styleFunction;
      this.olSelectObject.getFeatures().forEach(feature => feature.setStyle(styleFunction));
    },
    selectionChanged() {
      if (this.applyingStoreSelection) {
        return;
      }
      this.selectedFeatures = this.olSelectObject.getFeatures().getArray()
        .map(feature => this.format.writeFeatureObject(feature));
    },
    applyStoreSelection(value) {
      if (!this.olSelectObject) {
        return;
      }

      if (value.some(feature => feature.id === undefined)) {
        return;
      }

      let collection = this.olSelectObject.getFeatures();
      let wantedIds = value.map(feature => feature.id);

      this.applyingStoreSelection = true;
      try {
        for (let i = collection.getLength() - 1; i >= 0; i--) {
          if (!wantedIds.includes(collection.item(i).getId())) {
            collection.removeAt(i);
          }
        }

        let selectedIds = collection.getArray().map(feature => feature.getId());
        wantedIds.forEach(id => {
          if (selectedIds.includes(id)) {
            return;
          }
          let olFeature = this.findFeatureById(id);
          if (olFeature) {
            collection.push(olFeature);
          }
        });
      } finally {
        this.applyingStoreSelection = false;
      }
    },

    findFeatureById(id) {
      let map = this.olSelectObject.getMap();
      if (!map) {
        return null;
      }

      let found = null;
      map.getLayers().forEach(layer => {
        if (found || typeof layer.getSource !== 'function') {
          return;
        }
        let source = layer.getSource();
        if (source && typeof source.getFeatureById === 'function') {
          found = source.getFeatureById(id);
        }
      });

      return found;
    }
  },
  mounted() {
    this.olSelectObject = markRaw(this.$refs.olSelect.select);

    if (this.viewerContext) {
      this.viewerContext.register(this.selectTargetName, this.olSelectObject.getFeatures());
    }

    this.applySelectStyle(this.styleFunction);
    this.olSelectObject.getFeatures().on(['add', 'remove'], this.selectionChanged);
    this.applyStoreSelection(this.selectedFeatures);
  },
  beforeUnmount() {
    if (this.olSelectObject) {
      this.olSelectObject.getFeatures().un(['add', 'remove'], this.selectionChanged);
    }
    if (this.viewerContext) {
      this.viewerContext.unregister(this.selectTargetName);
    }
  }
};
</script>
