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

import * as buefy from 'buefy';

/**
 * In Vue 2 mode, @vue/compat rewrites the `modelValue` prop a `v-model` compiles
 * to back into the Vue 2 `value` prop, for every component. That is what the
 * app's own components still expect, but Buefy 3 components are plain Vue 3
 * components declaring `modelValue`: the rewrite makes `v-model` on any `<b-*>`
 * component a one-way binding that never updates.
 *
 * @vue/compat resolves COMPONENT_V_MODEL against the compat config of the
 * component receiving the v-model, so opting Buefy's components out one by one
 * fixes them without changing the contract of ours.
 *
 * This can be dropped once the app's own components use the Vue 3 `modelValue` /
 * `update:modelValue` contract and COMPONENT_V_MODEL can be disabled globally.
 */
export default function optOutBuefyFromVModelCompat() {
  for (const [name, exported] of Object.entries(buefy)) {
    if (name.startsWith('B') && exported && typeof exported === 'object') {
      exported.compatConfig = { ...exported.compatConfig, COMPONENT_V_MODEL: false };
    }
  }
}
