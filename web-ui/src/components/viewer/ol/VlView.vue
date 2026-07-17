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
import View from 'ol/View';

export default {
  name: 'vl-view',
  inject: ['vlMapCtx'],
  props: {
    center: {type: Array, default: () => [0, 0]},
    zoom: {type: Number, default: 0},
    rotation: {type: Number, default: 0},
    maxZoom: Number,
    maxResolution: Number,
    extent: Array,
    projection: String
  },
  emits: ['update:center', 'update:zoom', 'update:rotation', 'mounted'],
  created() {
    this.$view = new View({
      center: this.center,
      zoom: this.zoom,
      rotation: this.rotation,
      maxZoom: this.maxZoom,
      maxResolution: this.maxResolution,
      extent: this.extent,
      projection: this.projection
    });
    this.vlMapCtx.map.setView(this.$view);

    this.$view.on('change:center', () => {
      let center = this.$view.getCenter();
      if (!this.sameCoords(center, this.center)) {
        this.$emit('update:center', center);
      }
    });
    this.$view.on('change:resolution', () => {
      let zoom = this.$view.getZoom();
      if (zoom !== this.zoom) {
        this.$emit('update:zoom', zoom);
      }
    });
    this.$view.on('change:rotation', () => {
      let rotation = this.$view.getRotation();
      if (rotation !== this.rotation) {
        this.$emit('update:rotation', rotation);
      }
    });

    this.$createPromise = Promise.resolve(this.$view);
  },
  computed: {
    // reactive mirror kept for components receiving this component as prop (e.g. FollowPanel)
    viewCenter() {
      return this.center;
    }
  },
  watch: {
    center(value) {
      if (value && !this.sameCoords(value, this.$view.getCenter())) {
        this.$view.setCenter(value);
      }
    },
    zoom(value) {
      if (value !== null && value !== this.$view.getZoom()) {
        this.$view.setZoom(value);
      }
    },
    rotation(value) {
      if (value !== null && value !== this.$view.getRotation()) {
        this.$view.setRotation(value);
      }
    }
  },
  mounted() {
    this.$emit('mounted');
  },
  methods: {
    sameCoords(a, b) {
      if (!a || !b) {
        return a === b;
      }
      return a.length === b.length && a.every((val, idx) => val === b[idx]);
    },
    animate(...args) {
      return new Promise(resolve => {
        this.$view.animate(...args, complete => resolve(complete));
      });
    },
    fit(geometryOrExtent, options = {}) {
      return new Promise(resolve => {
        this.$view.fit(geometryOrExtent, {...options, callback: complete => resolve(complete)});
      });
    },
    calculateExtent(...args) {
      return this.$view.calculateExtent(...args);
    }
  },
  render() {
    return null;
  }
};
</script>
