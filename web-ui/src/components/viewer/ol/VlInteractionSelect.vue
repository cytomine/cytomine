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
import Select from 'ol/interaction/Select';
import Collection from 'ol/Collection';
import GeoJSON from 'ol/format/GeoJSON';
import VectorLayer from 'ol/layer/Vector';

import {register, unregister} from './identity-map';

export default {
  name: 'vl-interaction-select',
  inject: ['vlMapCtx'],
  props: {
    ident: String,
    filter: {type: Function, default: () => true},
    features: {type: Array, default: () => []},
    toggleCondition: Function,
    removeCondition: Function,
    multi: {type: Boolean, default: false}
  },
  emits: ['update:features'],
  beforeCreate() {
    this._styleTarget = {
      setStyle: style => {
        this._styleFn = style;
        if (this.$interaction) {
          this.$interaction.getFeatures().forEach(feature => feature.changed());
        }
      }
    };
  },
  provide() {
    return {vlStyleTarget: this._styleTarget};
  },
  created() {
    this._format = new GeoJSON();
    this._collection = new Collection();

    this.$interaction = new Select({
      features: this._collection,
      filter: (feature, layer) => this.filter(feature, layer),
      toggleCondition: this.toggleCondition,
      removeCondition: this.removeCondition,
      multi: this.multi,
      style: (feature, resolution) => this._styleFn ? this._styleFn(feature, resolution) : null
    });

    this.$interaction.on('select', () => {
      let features = this._collection.getArray()
        .map(feature => this._format.writeFeatureObject(feature));
      this.$emit('update:features', features);
    });

    this.vlMapCtx.map.addInteraction(this.$interaction);

    if (this.ident) {
      register(this.ident, this._collection);
    }

    this.$createPromise = Promise.resolve(this.$interaction);
  },
  watch: {
    features: {
      deep: true,
      handler(value) {
        this.reconcile(value || []);
      }
    }
  },
  beforeUnmount() {
    if (this.ident) {
      unregister(this.ident, this._collection);
    }
    this.vlMapCtx.map.removeInteraction(this.$interaction);
  },
  methods: {
    findFeatureById(id) {
      let layers = this.vlMapCtx.map.getLayers().getArray();
      for (let layer of layers) {
        if (layer instanceof VectorLayer && layer.getSource()) {
          let feature = layer.getSource().getFeatureById(id);
          if (feature) {
            return feature;
          }
        }
      }
      return null;
    },
    reconcile(plainFeatures) {
      let wantedIds = plainFeatures.filter(f => f.id !== undefined).map(f => f.id);
      let current = this._collection.getArray().slice();

      for (let feature of current) {
        if (feature.getId() !== undefined && !wantedIds.includes(feature.getId())) {
          this._collection.remove(feature);
        }
      }

      let currentIds = this._collection.getArray().map(f => f.getId());
      for (let id of wantedIds) {
        if (!currentIds.includes(id)) {
          let feature = this.findFeatureById(id);
          if (feature) {
            this._collection.push(feature);
          }
        }
      }
    }
  }
};
</script>
