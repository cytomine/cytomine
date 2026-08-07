import Vue, { createApp } from 'vue';
import axios from 'axios';
import constants from '@/utils/constants.js';
import { getKeycloak } from './keycloak.js';

Vue.configureCompat({ MODE: 2, COMPONENT_V_MODEL: false }); // TODO: remove when migration is done

import i18n from './lang.js';
import router from './routes.js';
import store from './store/store.js';

import Buefy from 'buefy';
import optOutBuefyFromVue2Compat from '@/utils/buefy-compat.js';
optOutBuefyFromVue2Compat();

import Notifications from '@kyvg/vue3-notification';

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

  const keycloak = getKeycloak();

  keycloak.init({ onLoad: 'login-required' }).then(() => {
    const app = createApp(App);
    app.use(i18n);
    app.use(router);
    app.use(store);
    app.use(Buefy, { defaultIconPack: 'fas' });
    app.use(Notifications);
    app.mount('#app');
  });
});
