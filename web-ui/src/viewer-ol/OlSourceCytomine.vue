<!-- The element carries nothing: like `vue3-openlayers`' own `<ol-source-*>`
     components, this one exists only to own an ol source. -->
<template><div class="ol-source-cytomine" /></template>

<script>
/**
 * Replaces `src/vuelayers-suppl/cytomine-source/`.
 *
 * `vue3-openlayers` ships no component for a custom `TileImage`, so this one
 * plugs into `<ol-tile-layer>` through the `tileLayer` key that component
 * provides, the same way its own `<ol-source-*>` components do.
 */
import { markRaw } from 'vue';

import CytomineTileSource from './cytomine-tile-source.js';

export default {
  name: 'ol-source-cytomine',
  inject: {
    // `<ol-tile-layer>` provides a `shallowRef`, which the Options API unwraps
    // on the way in, so this is the ol layer itself.
    tileLayer: { from: 'tileLayer', default: null }
  },
  props: {
    url: { type: String, required: true },
    extent: { type: Array, required: true },
    tileSize: { type: [Number, Array], required: true },
    nbResolutions: { type: Number, required: true },
    projection: { type: String, required: true },
    tileLoadFunction: { type: Function, default: undefined },
    crossOrigin: { type: String, default: undefined },
    transition: { type: Number, default: undefined }
  },
  emits: ['ready'],
  data() {
    return {
      source: null
    };
  },
  watch: {
    url(url) {
      this.source.setUrlTemplate(url);
      this.source.refresh();
    },
    tileLoadFunction(tileLoadFunction) {
      this.source.setTileLoadFunction(tileLoadFunction);
      this.source.refresh();
    }
  },
  created() {
    this.source = markRaw(new CytomineTileSource({
      url: this.url,
      extent: this.extent,
      tileSize: this.tileSize,
      nbResolutions: this.nbResolutions,
      projection: this.projection,
      tileLoadFunction: this.tileLoadFunction,
      crossOrigin: this.crossOrigin,
      transition: this.transition
    }));

    if (this.tileLayer) {
      this.tileLayer.setSource(this.source);
    }

    this.$emit('ready', this.source);
  },
  beforeUnmount() {
    if (this.tileLayer) {
      this.tileLayer.setSource(null);
    }
  }
};
</script>
