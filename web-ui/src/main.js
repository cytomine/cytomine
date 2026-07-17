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

import {createApp} from 'vue';
import axios from 'axios';
import moment from 'moment';
import html2canvas from 'html2canvas';

import constants from '@/utils/constants.js';
import eventBus from '@/utils/event-bus.js';
import {clickOutside, shortkey} from '@/utils/directives.js';
import VeeValidateShim from '@/utils/vee-validate-shim.js';

import router from './routes.js';
import i18n from './lang.js';
import store from './store/store.js';

import Buefy from '@ntohq/buefy-next';
import Notifications from '@kyvg/vue3-notification';
import FloatingVue from 'floating-vue';
import 'floating-vue/dist/style.css';

import OlComponents from '@/components/viewer/ol';

import Chart from 'chart.js';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import ChartZoom from 'chartjs-plugin-zoom';
Chart.plugins.unregister(ChartZoom);
Chart.plugins.unregister(ChartDataLabels);
Chart.helpers.merge(Chart.defaults.global.plugins.datalabels, {
  anchor: 'end',
  align: 'end',
  offset: 5,
  clamp: true
});

import App from './App.vue';

// Load configuration before initializing Keycloak
axios.get('configuration.json').then(response => {
  const settings = response.data;
  for (let i in settings) {
    if (Object.prototype.hasOwnProperty.call(constants, i)
      || i.includes('_NAMESPACE') || i.includes('_VERSION') || i.includes('_ENABLED')) {
      constants[i] = settings[i];
    }
  }

  // Now import and initialize Keycloak with loaded config
  import('./keycloak').then(module => {
    const Keycloak = module.default;
    const {getKeycloak} = module;

    getKeycloak()
      .init({
        onLoad: 'login-required'
      })
      .then(() => {
        const app = createApp(App);

        app.use(router);
        app.use(i18n);
        app.use(store);
        app.use(Keycloak);
        app.use(Buefy, {defaultIconPack: 'fas'});
        app.use(Notifications);
        app.use(FloatingVue);
        app.use(VeeValidateShim);
        app.use(OlComponents);

        app.directive('click-outside', clickOutside);
        app.directive('shortkey', shortkey);

        app.config.globalProperties.$eventBus = eventBus;
        app.config.globalProperties.$moment = moment;
        app.config.globalProperties.$html2canvas = async (el, options = {}) => {
          const canvas = await html2canvas(el, options);
          return options.type === 'dataURL' ? canvas.toDataURL() : canvas;
        };

        app.mount('#app');
      });
  });
});
