import Vue from 'vue';
import axios from 'axios';
import constants from '@/utils/constants.js';
import { getKeycloak } from '@/keycloak.js';

import VueRouter from 'vue-router';
import router from './routes.js';
Vue.use(VueRouter);

import i18n from './lang.js';

import store from './store/store.js';

import Buefy from 'buefy';
Vue.use(Buefy, { defaultIconPack: 'fas' });

import VeeValidate, { Validator } from 'vee-validate';
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

import { vOnClickOutside } from '@vueuse/components';
Vue.directive('click-outside', vOnClickOutside);

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

import 'chart.js/auto';

import App from './App.vue';

Vue.config.productionTip = false;

// When Keycloak ends a flow it did not start from this page (e.g. an LTI launch brokered through
// /lti/login-init), it redirects here with OIDC response parameters in the query string
// (?session_state=...&iss=...&code=...). keycloak-js uses the fragment response mode and ignores them,
// so they would end up in the redirect_uri of its own login request, which Keycloak 26 rejects
// ("Redirect URI rejected: contains forbidden OIDC parameter 'code' in query"). Remove them before
// initializing Keycloak, but keep other parameters (e.g. the deep-linking picker's ctx).
function stripOidcCallbackParams() {
  const url = new URL(window.location.href);
  if (!url.searchParams.has('code')) {
    return;
  }
  ['code', 'state', 'session_state', 'iss'].forEach(param => url.searchParams.delete(param));
  window.history.replaceState(window.history.state, '', url.pathname + url.search + url.hash);
}

stripOidcCallbackParams();

// Load configuration before initializing Keycloak
axios.get('/configuration.json').then(response => {
  const settings = response.data;
  for (let i in settings) {
    if (Object.prototype.hasOwnProperty.call(constants, i)
      || i.includes('_NAMESPACE') || i.includes('_VERSION') || i.includes('_ENABLED')) {
      constants[i] = settings[i];
    }
  }

  // Now initialize Keycloak with loaded config
  const keycloak = getKeycloak();
  keycloak
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
