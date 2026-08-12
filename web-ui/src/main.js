import Vue, { createApp } from 'vue';
import axios from 'axios';
import constants from '@/utils/constants.js';
import { getKeycloak } from './keycloak.js';

Vue.configureCompat({ MODE: 2, COMPONENT_V_MODEL: false }); // TODO: to delete when removing @vue/compat

import i18n from './lang.js';
import router from './routes.js';
import store from './store/store.js';

import Buefy from 'buefy';
import optOutBuefyFromVue2Compat from '@/utils/buefy-compat.js';
optOutBuefyFromVue2Compat(); // TODO: to delete when removing @vue/compat

import Notifications from '@kyvg/vue3-notification';
import FloatingVue from 'floating-vue';

import ViewerOpenLayers from './viewer-ol';
Vue.use(ViewerOpenLayers);

import 'chart.js/auto';
import 'vue-color/style.css';
import '@he-tree/vue/style/default.css';
import optOutVueColorFromVue2Compat from '@/utils/vue-color-compat.js';
optOutVueColorFromVue2Compat(); // TODO: to delete when removing @vue/compat

import { vOnClickOutside } from '@vueuse/components';

import App from './App.vue';

Vue.config.productionTip = false;

// Load configuration before initializing Keycloak
axios.get('/configuration.json').then(response => {
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
    app.use(FloatingVue);
    app.use(Notifications);
    app.directive('on-click-outside', vOnClickOutside);
    app.mount('#app');
  });
});
