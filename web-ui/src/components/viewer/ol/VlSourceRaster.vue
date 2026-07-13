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

<script>
import RasterSource from 'ol/source/Raster';

export default {
  name: 'vl-source-raster',
  inject: ['vlLayerCtx'],
  props: {
    sources: {type: Array},
    operation: {type: Function},
    lib: {type: Object}
  },
  emits: ['mounted'],
  created() {
    this.$source = new RasterSource({
      sources: this.sources,
      operation: this.operation,
      lib: this.lib
    });
    this.vlLayerCtx.setSource(this.$source);
    this.$createPromise = Promise.resolve(this.$source);
  },
  watch: {
    operation() {
      this.$source.setOperation(this.operation, this.lib);
    },
    lib() {
      this.$source.setOperation(this.operation, this.lib);
    }
  },
  mounted() {
    this.$emit('mounted');
  },
  beforeUnmount() {
    this.vlLayerCtx.setSource(null);
  },
  render() {
    return null;
  }
};
</script>
