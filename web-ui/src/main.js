import Vue from 'vue';
import axios from 'axios';
import constants from '@/utils/constants.js';

import VueRouter from 'vue-router';
import router from './routes.js';
Vue.use(VueRouter);

import i18n from './lang.js';

import store from './store/store.js';

import Buefy from 'buefy';
Vue.use(Buefy, {defaultIconPack: 'fas'});

import VeeValidate, {Validator} from 'vee-validate';
Validator.extend('positive', value => Number(value) > 0);
Vue.use(VeeValidate, {
  i18nRootKey: 'validations',
  i18n,
  inject: false
});

import Notifications from 'vue-notification';
Vue.use(Notifications);

import VTooltip from 'v-tooltip';
Vue.use(VTooltip);

import VueShortKey from 'vue-shortkey';
Vue.use(VueShortKey, {
  prevent: [
    'input[type=text]',
    'input[type=password]',
    'input[type=search]',
    'input[type=email]',
    'textarea',
    '.ql-editor'
  ]
});

import VueHtml2Canvas from 'vue-html2canvas';

Vue.use(VueHtml2Canvas);

import * as vClickOutside from 'v-click-outside-x';
Vue.use(vClickOutside);

import VueLayers from 'vuelayers';
import CytomineSource from './vuelayers-suppl/cytomine-source';
import RasterSource from './vuelayers-suppl/raster-source';
import TranslateInteraction from './vuelayers-suppl/translate-interaction';
import RotateInteraction from './vuelayers-suppl/rotate-interaction';
import ModifyInteraction from './vuelayers-suppl/modify-interaction';
import RescaleInteraction from './vuelayers-suppl/rescale-interaction';
Vue.use(VueLayers);
Vue.use(CytomineSource);
Vue.use(RasterSource);
Vue.use(TranslateInteraction);
Vue.use(RotateInteraction);
Vue.use(ModifyInteraction);
Vue.use(RescaleInteraction);

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

Vue.config.productionTip = false;
Vue.prototype.$eventBus = new Vue();

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
    Vue.use(Keycloak);

    Vue.$keycloak
      .init({
        onLoad: 'login-required'
      })
      .then(() => {
        new Vue({
          render: h => h(App),
          router,
          store,
          i18n
        }).$mount('#app');
      });
  });
});
