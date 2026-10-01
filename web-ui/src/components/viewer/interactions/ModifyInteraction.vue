<!-- Renderless: this component only owns whichever ol interaction the active
     edit tool needs. `vue3-openlayers` has no component for three of the four
     (translate, rotate, rescale) and its `<ol-interaction-modify>` builds a
     plain `ol/interaction/Modify`, not the rectangle-aware subclass. -->
<template><div class="modify-interaction" /></template>

<script>
import { markRaw } from 'vue';

import eventBus from '@/utils/event-bus';

import WKT from 'ol/format/WKT';
import Translate from 'ol/interaction/Translate';
import { singleClick } from 'ol/events/condition';
import RotateFeatureInteraction from 'ol-rotate-feature';
import RescaleFeatureInteraction from 'ol-rescale-feature';

import { Action } from '@/utils/annotation-utils.js';
import { isRectangle } from '@/utils/geometry-utils';
import ModifyInteractionOl from '@/viewer-ol/modify.js';
import { injectViewerContext } from '@/viewer-ol/context.js';

const EDIT_EVENTS = {
  modify: ['modifystart', 'modifyend'],
  translate: ['translatestart', 'translateend'],
  rescale: ['rescalestart', 'rescaleend'],
  rotate: ['rotatestart', 'rotateend']
};

export default {
  name: 'modify-interaction',
  inject: {
    ...injectViewerContext,
    olMap: { from: 'map', default: null }
  },
  props: {
    index: String
  },
  data() {
    return {
      format: new WKT(),
      interaction: null
    };
  },
  computed: {
    imageModule() {
      return this.$store.getters['currentProject/imageModule'](this.index);
    },
    imageWrapper() {
      return this.$store.getters['currentProject/currentViewer'].images[this.index];
    },
    image() {
      return this.imageWrapper.imageInstance;
    },
    selectSource() {
      return `select-target-${this.index}`;
    },
    /** The `Collection` of features `SelectInteraction` has selected. */
    selectedOlFeatures() {
      return this.viewerContext ? this.viewerContext.resolve(this.selectSource) : null;
    },
    activeEditTool() {
      return this.imageWrapper.draw.activeEditTool;
    },
    ongoingEdit: {
      get() {
        return this.imageWrapper.draw.ongoingEdit;
      },
      set(value) {
        this.$store.commit(this.imageModule + 'setOngoingEdit', value);
      }
    },
    deleteCondition() {
      return function (mapBrowserEvent) {
        return mapBrowserEvent.originalEvent.ctrlKey && singleClick(mapBrowserEvent);
      };
    },
    insertVertexCondition() {
      return function () {
        // `Modify.features_` is a plain array since ol 6, not the Collection
        // that was passed in, so there is no `getArray()` any more.
        return !this.features_.every(function (feature) {
          return isRectangle(feature.getGeometry());
        });
      };
    }
  },
  watch: {
    activeEditTool: 'rebuildInteraction',
    selectedOlFeatures: 'rebuildInteraction'
  },
  methods: {
    buildInteraction() {
      let features = this.selectedOlFeatures;
      if (!this.olMap || !features || !EDIT_EVENTS[this.activeEditTool]) {
        return;
      }

      let interaction;
      switch (this.activeEditTool) {
        case 'modify':
          interaction = new ModifyInteractionOl({
            features,
            deleteCondition: this.deleteCondition,
            insertVertexCondition: this.insertVertexCondition
          });
          break;
        case 'translate':
          interaction = new Translate({ features });
          break;
        case 'rescale':
          interaction = new RescaleFeatureInteraction({ features });
          break;
        case 'rotate':
          interaction = new RotateFeatureInteraction({ features });
          break;
      }

      let [startEvent, endEvent] = EDIT_EVENTS[this.activeEditTool];
      interaction.on(startEvent, this.startEdit);
      interaction.on(endEvent, this.endEdit);

      this.olMap.addInteraction(interaction);
      this.interaction = markRaw(interaction);
    },

    destroyInteraction() {
      if (!this.interaction) {
        return;
      }
      if (this.olMap) {
        this.olMap.removeInteraction(this.interaction);
      }
      this.interaction = null;
    },

    rebuildInteraction() {
      this.destroyInteraction();
      this.buildInteraction();
    },

    startEdit() {
      this.ongoingEdit = true;
    },
    async endEdit({ features }) {
      features.forEach(async feature => {
        if (!feature.get('annot')) {
          return;
        }

        let annot = feature.get('annot').clone();
        let oldLocation = annot.location;
        try {
          annot.location = this.format.writeFeature(feature);
          annot.terms = annot.term; // HACK for reviewed annotation (unconsistent behaviour)
          await annot.save();
          eventBus.emit('editAnnotation', annot);
          eventBus.emit('reloadAnnotationCrop', annot);
          this.$store.commit(this.imageModule + 'addAction', { annot, type: Action.UPDATE });
        } catch (err) {
          console.log(err);
          this.$notify({ type: 'error', text: this.$t('notif-error-annotation-update') });
          annot.location = oldLocation;
          feature.setGeometry(this.format.readGeometry(annot.location));
        }
      });
      this.ongoingEdit = false;
    },
  },
  mounted() {
    this.buildInteraction();
  },
  beforeUnmount() {
    this.destroyInteraction();
  }
};
</script>
