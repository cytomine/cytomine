import Vue, { createApp } from 'vue';
import axios from 'axios';
import constants from '@/utils/constants.js';

// Keep Vue 2 runtime behavior by default while migrating incrementally.
// COMPONENT_V_MODEL is off: the app's own components use the Vue 3
// `modelValue` / `update:modelValue` v-model contract (issue 15), so compat
// must not rewrite it back to the Vue 2 `value` / `input` pair.
Vue.configureCompat({ MODE: 2, COMPONENT_V_MODEL: false });

import i18n from './lang.js';
import router from './routes.js';
import store from './store/store.js';

import Buefy from 'buefy';
import optOutBuefyFromVue2Compat from '@/utils/buefy-compat.js';
optOutBuefyFromVue2Compat();

import Notifications from '@kyvg/vue3-notification';

import VTooltip from 'v-tooltip';
Vue.use(VTooltip);

import { vOnClickOutside } from '@vueuse/components';
Vue.directive('click-outside', vOnClickOutside);

import ViewerOpenLayers from './viewer-ol';
Vue.use(ViewerOpenLayers);

import 'chart.js/auto';

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

  // Now import and initialize Keycloak with loaded config
  import('./keycloak').then(module => {
    const Keycloak = module.default;
    Vue.use(Keycloak);

    Vue.$keycloak
      .init({
        onLoad: 'login-required'
      })
      .then(() => {
        const app = createApp(App);
        app.use(i18n);
        app.use(router);
        app.use(store);
        app.use(Buefy, { defaultIconPack: 'fas' });
        app.use(Notifications);
        app.mount('#app');
      });
  });
});
