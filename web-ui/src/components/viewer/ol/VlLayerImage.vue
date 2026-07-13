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
  <div style="display: none"><slot /></div>
</template>

<script>
import ImageLayer from 'ol/layer/Image';

export default {
  name: 'vl-layer-image',
  inject: ['vlMapCtx'],
  props: {
    extent: Array,
    visible: {type: Boolean, default: true}
  },
  emits: ['mounted'],
  beforeCreate() {
    this._layerCtx = {layer: null, setSource: () => {}};
  },
  provide() {
    return {vlLayerCtx: this._layerCtx};
  },
  created() {
    this.$layer = new ImageLayer({
      extent: this.extent,
      visible: this.visible
    });
    this._layerCtx.layer = this.$layer;
    this._layerCtx.setSource = source => this.$layer.setSource(source);
    this.vlMapCtx.map.addLayer(this.$layer);
    this.$createPromise = Promise.resolve(this.$layer);
  },
  watch: {
    visible(value) {
      this.$layer.setVisible(value);
    }
  },
  mounted() {
    this.$emit('mounted');
  },
  beforeUnmount() {
    this.vlMapCtx.map.removeLayer(this.$layer);
  }
};
</script>
