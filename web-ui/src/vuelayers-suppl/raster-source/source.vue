<script>
/**
 * @module raster-source/source
 */
import RasterSource from 'ol/source/Raster';
import tileSource from 'vuelayers/lib/mixin/image-source';

const props = {
  sources: {type: Array},
  operation: {type: Function},
  lib: {type: Object},
};

const methods = {
  createSource() {
    return new RasterSource({
      sources: this.sources,
      operation: this.operation,
      lib: this.lib,
    });
  }
};

const watch = {
  operation() {
    this.$source.setOperation(this.operation, this.lib);
  },
  lib() {
    this.$source.setOperation(this.operation, this.lib);
  }
};

export default {
  name: 'vl-source-raster',
  mixins: [tileSource],
  props,
  methods,
  watch
};
</script>
