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
import RotateFeatureInteraction from 'ol-rotate-feature';
import {getIdent} from './identity-map';

export default {
  name: 'vl-interaction-rotate',
  inject: ['vlMapCtx'],
  props: {
    source: {type: String, required: true},
    angle: {type: Number, default: 0}
  },
  emits: ['rotatestart', 'rotateend'],
  created() {
    this.$interaction = new RotateFeatureInteraction({
      features: getIdent(this.source),
      angle: this.angle
    });
    this.$interaction.on('rotatestart', evt => this.$emit('rotatestart', evt));
    this.$interaction.on('rotateend', evt => this.$emit('rotateend', evt));
    this.vlMapCtx.map.addInteraction(this.$interaction);
    this.$createPromise = Promise.resolve(this.$interaction);
  },
  beforeUnmount() {
    this.vlMapCtx.map.removeInteraction(this.$interaction);
  },
  render() {
    return null;
  }
};
</script>
