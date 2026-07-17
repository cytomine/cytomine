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

// Vue 2 logged errors thrown during (async) rendering; in Vue 3 they become
// uncaught exceptions that kill the jest process. Route them to a warning, as
// the old test suites rely on partial renders against incomplete mocks.
const {config} = require('./vtu-compat');

config.global.config.errorHandler = err => {
  // eslint-disable-next-line no-console
  console.warn('[vue error handler]', err && err.message ? err.message : err);
};
config.global.config.warnHandler = () => {};

// test-utils v1 stubs rendered their default slot; v2 hides it by default
config.global.renderStubDefaultSlot = true;

// $moment used to be provided by vue-moment; it is now a global property
// registered in main.js, so provide it here for component tests
const moment = require('moment');
config.global.mocks.$moment = moment;
