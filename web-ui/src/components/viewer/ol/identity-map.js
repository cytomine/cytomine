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
 * Registry mapping string identifiers to OpenLayers objects (sources, feature
 * collections), so interactions can reference them by name across component
 * subtrees (replaces the VueLayers IdentityMap).
 */

const registry = new Map();

export function register(ident, value) {
  registry.set(ident, value);
}

export function unregister(ident, value) {
  if (registry.get(ident) === value) {
    registry.delete(ident);
  }
}

export function getIdent(ident) {
  return registry.get(ident);
}
