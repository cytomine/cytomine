import Notifications from '@kyvg/vue3-notification';
import { vOnClickOutside } from '@vueuse/components';
import axios from 'axios';
import Buefy from 'buefy';
import FloatingVue from 'floating-vue';
import { createApp } from 'vue';

import App from '@/App.vue';
import { getKeycloak } from '@/keycloak.js';
import i18n from '@/lang.js';
import router from '@/routes.js';
import store from '@/store/store.js';
import constants from '@/utils/constants.js';
import ViewerOpenLayers from '@/viewer-ol';

import '@he-tree/vue/style/default.css';
import 'chart.js/auto';
import 'vue-color/style.css';

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
    app.use(ViewerOpenLayers);
    app.directive('click-outside', vOnClickOutside);
    app.mount('#app');
  });
});
