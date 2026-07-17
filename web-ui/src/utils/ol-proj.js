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

// Replacements for the projection helpers previously imported from
// 'vuelayers/lib/ol-ext'.

import Projection from 'ol/proj/Projection';
import {addProjection, get} from 'ol/proj';

export function createProj({code, units, extent}) {
  return new Projection({code, units, extent});
}

export function addProj(projection) {
  addProjection(projection);
}

export function getProj(code) {
  return get(code);
}
