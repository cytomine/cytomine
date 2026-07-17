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
import Draw from 'ol/interaction/Draw';

import {getIdent} from './identity-map';

export default {
  name: 'vl-interaction-draw',
  inject: ['vlMapCtx'],
  props: {
    source: {type: String, required: true},
    type: {type: String, required: true},
    freehand: {type: Boolean, default: false},
    freehandCondition: {type: Function, default: undefined},
    geometryFunction: {type: Function, default: undefined}
  },
  emits: ['drawstart', 'drawend'],
  created() {
    this.createInteraction();
  },
  watch: {
    type() {
      this.scheduleRecreate();
    },
    freehand() {
      this.scheduleRecreate();
    },
    geometryFunction() {
      this.scheduleRecreate();
    }
  },
  beforeUnmount() {
    this.destroyInteraction();
  },
  methods: {
    createInteraction() {
      if (!this.type) {
        return;
      }
      this.$interaction = new Draw({
        source: getIdent(this.source),
        type: this.type,
        freehand: this.freehand,
        freehandCondition: this.freehandCondition,
        geometryFunction: this.geometryFunction
      });
      this.$interaction.on('drawstart', evt => this.$emit('drawstart', evt));
      this.$interaction.on('drawend', evt => this.$emit('drawend', evt));
      this.vlMapCtx.map.addInteraction(this.$interaction);
    },
    destroyInteraction() {
      if (this.$interaction) {
        this.vlMapCtx.map.removeInteraction(this.$interaction);
        this.$interaction = null;
      }
    },
    async scheduleRecreate() {
      if (this._recreateScheduled) {
        return;
      }
      this._recreateScheduled = true;
      await this.$nextTick(); // let all prop updates settle before recreating
      this._recreateScheduled = false;
      this.destroyInteraction();
      this.createInteraction();
    }
  },
  render() {
    return null;
  }
};
</script>
