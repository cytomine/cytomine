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
import VectorSource from 'ol/source/Vector';
import {all as allStrategy} from 'ol/loadingstrategy';

import {register, unregister} from './identity-map';

export default {
  name: 'vl-source-vector',
  inject: ['vlLayerCtx'],
  props: {
    ident: String,
    loaderFactory: Function,
    strategyFactory: Function,
    url: String // kept for API compatibility (unused: the loader takes care of fetching)
  },
  emits: ['mounted'],
  created() {
    this.$source = new VectorSource({
      loader: this.loaderFactory ? this.loaderFactory() : undefined,
      strategy: this.strategyFactory ? this.strategyFactory() : allStrategy
    });
    this.vlLayerCtx.setSource(this.$source);
    if (this.ident) {
      register(this.ident, this.$source);
    }
    this.$createPromise = Promise.resolve(this.$source);
  },
  mounted() {
    this.$emit('mounted');
  },
  beforeUnmount() {
    if (this.ident) {
      unregister(this.ident, this.$source);
    }
    this.vlLayerCtx.setSource(null);
  },
  methods: {
    addFeature(feature) {
      this.$source.addFeature(feature);
    },
    removeFeature(feature) {
      this.$source.removeFeature(feature);
    },
    getFeatureById(id) {
      return this.$source.getFeatureById(id);
    },
    clearFeatures(fast) {
      this.$source.clear(fast);
    },
    clear(fast) {
      this.$source.clear(fast);
    }
  }
};
</script>
