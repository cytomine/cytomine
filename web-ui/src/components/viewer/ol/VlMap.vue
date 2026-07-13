<!-- Copyright (c) 2009-2022. Authors: see NOTICE file.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.-->

<template>
  <div class="vl-map map">
    <slot />
  </div>
</template>

<script>
import Map from 'ol/Map';
import {defaults as defaultControls} from 'ol/control';
import {defaults as defaultInteractions} from 'ol/interaction';

import 'ol/ol.css';

export default {
  name: 'vl-map',
  props: {
    dataProjection: String, // kept for API compatibility; view and data share the same pixel projection
    loadTilesWhileAnimating: {type: Boolean, default: false},
    loadTilesWhileInteracting: {type: Boolean, default: false},
    keyboardEventTarget: {default: undefined}
  },
  emits: ['pointermove', 'mounted'],
  beforeCreate() {
    this.$map = null;
    this._mapCtx = {
      map: null
    };
  },
  provide() {
    return {vlMapCtx: this._mapCtx};
  },
  created() {
    this.$map = new Map({
      controls: defaultControls(),
      interactions: defaultInteractions(),
      layers: [],
      loadTilesWhileAnimating: this.loadTilesWhileAnimating,
      loadTilesWhileInteracting: this.loadTilesWhileInteracting,
      keyboardEventTarget: this.keyboardEventTarget
    });
    this._mapCtx.map = this.$map;
    this.$map.on('pointermove', evt => this.$emit('pointermove', evt));
    this.$createPromise = Promise.resolve(this.$map);
  },
  mounted() {
    this.$map.setTarget(this.$el);
    this.$map.updateSize();
    this.$emit('mounted');
  },
  beforeUnmount() {
    this.$map.setTarget(null);
  },
  methods: {
    updateSize() {
      this.$map.updateSize();
    }
  }
};
</script>

<style>
.vl-map {
  position: relative;
  width: 100%;
  height: 100%;
}
</style>
