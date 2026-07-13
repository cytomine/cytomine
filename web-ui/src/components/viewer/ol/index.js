/*
* Copyright (c) 2009-2022. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * In-repo Vue 3 wrappers around OpenLayers, replacing the (Vue 2 only)
 * VueLayers library and the custom plugins that used to live in
 * src/vuelayers-suppl. Only the component API surface used by the viewer is
 * implemented.
 */

import VlMap from './VlMap.vue';
import VlView from './VlView.vue';
import VlLayerTile from './VlLayerTile.vue';
import VlLayerVector from './VlLayerVector.vue';
import VlLayerImage from './VlLayerImage.vue';
import VlSourceCytomine from './VlSourceCytomine.vue';
import VlSourceVector from './VlSourceVector.vue';
import VlSourceRaster from './VlSourceRaster.vue';
import VlStyleFunc from './VlStyleFunc.vue';
import VlInteractionDraw from './VlInteractionDraw.vue';
import VlInteractionSelect from './VlInteractionSelect.vue';
import VlInteractionModify from './VlInteractionModify.vue';
import VlInteractionTranslate from './VlInteractionTranslate.vue';
import VlInteractionRotate from './VlInteractionRotate.vue';
import VlInteractionRescale from './VlInteractionRescale.vue';

const components = [
  VlMap,
  VlView,
  VlLayerTile,
  VlLayerVector,
  VlLayerImage,
  VlSourceCytomine,
  VlSourceVector,
  VlSourceRaster,
  VlStyleFunc,
  VlInteractionDraw,
  VlInteractionSelect,
  VlInteractionModify,
  VlInteractionTranslate,
  VlInteractionRotate,
  VlInteractionRescale
];

export default {
  install(app) {
    components.forEach(component => app.component(component.name, component));
  }
};
