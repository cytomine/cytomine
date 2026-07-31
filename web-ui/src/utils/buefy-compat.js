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
 * Buefy 3 is a plain Vue 3 library, but `configureCompat({MODE: 2})` in
 * `src/main.js` is global: @vue/compat applies its Vue 2 behaviours to every
 * component in the tree, Buefy's included, and several of them actively break
 * it. Per-component `compatConfig: {MODE: 3}` opts a component out of all of
 * them, which is what Buefy wants, while the app's own components keep MODE 2.
 *
 * The two that were breaking things concretely:
 *
 * - COMPONENT_V_MODEL rewrites the `modelValue` prop a `v-model` compiles to
 *   back into the Vue 2 `value` prop. Buefy 3 components declare `modelValue`,
 *   so `v-model` on any `<b-*>` was a one-way binding that never updated.
 *   compat resolves this flag against the component *receiving* the v-model,
 *   so opting Buefy out fixes it without changing the contract of ours.
 * - RENDER_FUNCTION makes `$slots` a Vue 2 style proxy that *calls* each slot
 *   function on property access, with no arguments. `<b-table>` renders a cell
 *   by testing `column.$slots.default`, so merely testing it invoked every
 *   `<b-table-column v-slot="{row}">` with `undefined` and threw. It applies to
 *   any Buefy component with a hand-written `render()` — `BTableColumn`,
 *   `BSlotComponent`.
 *
 * This whole file goes away with @vue/compat itself (issue 16), once the app's
 * own components use the Vue 3 `modelValue` / `update:modelValue` contract and
 * MODE 2 can be turned off globally.
 */
export default function optOutBuefyFromVue2Compat() {
  for (const [name, exported] of Object.entries(buefy)) {
    if (name.startsWith('B') && exported && typeof exported === 'object') {
      exported.compatConfig = { ...exported.compatConfig, MODE: 3 };
    }
  }
}
