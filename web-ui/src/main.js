import Vue from 'vue';
import axios from 'axios';
import constants from '@/utils/constants.js';

// Keep Vue 2 runtime behavior by default while migrating incrementally.
Vue.configureCompat({ MODE: 2 });

import VueRouter from 'vue-router';
import router from './routes.js';
Vue.use(VueRouter);

import i18n from './lang.js';
Vue.use(i18n);

import store from './store/store.js';

import Buefy from 'buefy';
import optOutBuefyFromVModelCompat from '@/utils/buefy-compat.js';
Vue.use(Buefy, { defaultIconPack: 'fas' });
optOutBuefyFromVModelCompat();

import Notifications from '@kyvg/vue3-notification';
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

import ViewerOpenLayers from './viewer-ol';
Vue.use(ViewerOpenLayers);

import 'chart.js/auto';

import App from './App.vue';

Vue.config.productionTip = false;

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
          store
        }).$mount('#app');
      });
  });
});
