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
import TileGrid from 'ol/tilegrid/TileGrid';
import {CustomTile} from 'ol/source/Zoomify';
import TileImage from 'ol/source/TileImage';

export default {
  name: 'vl-source-cytomine',
  inject: ['vlLayerCtx'],
  props: {
    projection: String,
    url: String,
    tileLoadFunction: Function,
    size: Array,
    extent: Array,
    nbResolutions: Number,
    transition: {type: Number, default: undefined},
    tileSize: {type: Array, default: () => [256, 256]},
    crossOrigin: String,
    cacheSize: Number
  },
  emits: ['mounted'],
  created() {
    const resolutions = [1];
    for (let i = 1; i <= this.nbResolutions; ++i) {
      resolutions.push(1 << i);
    }
    resolutions.reverse();

    // normalized tile grid, inspired from Zoomify
    const extent = this.extent;
    const tileGrid = new TileGrid({
      tileSize: this.tileSize,
      extent: extent,
      origin: [extent[0], extent[3]],
      resolutions: resolutions
    });

    this.$source = new TileImage({
      cacheSize: this.cacheSize,
      crossOrigin: this.crossOrigin,
      projection: this.projection,
      tileClass: CustomTile.bind(null, tileGrid),
      tileGrid: tileGrid,
      tileUrlFunction: this.createUrlFunc(),
      tileLoadFunction: this.tileLoadFunction,
      transition: this.transition
    });

    this.vlLayerCtx.setSource(this.$source);
    this.$createPromise = Promise.resolve(this.$source);
  },
  watch: {
    url() {
      if (this.$source) {
        this.$source.setTileUrlFunction(this.createUrlFunc());
        this.$source.setTileLoadFunction(this.tileLoadFunction);
        this.$source.refresh();
      }
    }
  },
  mounted() {
    this.$emit('mounted');
  },
  beforeUnmount() {
    this.vlLayerCtx.setSource(null);
  },
  methods: {
    createUrlFunc() {
      return tileCoord => {
        if (!tileCoord) {
          return undefined;
        }
        const [z, x, y] = tileCoord;
        // ol@5 tile rows increase upward: with a top-left origin the first
        // row below the origin is -1, hence the -y-1 conversion
        return this.url
          .replace('{z}', String(z))
          .replace('{x}', String(x))
          .replace('{y}', String(-y - 1));
      };
    }
  },
  render() {
    return null;
  }
};
</script>
