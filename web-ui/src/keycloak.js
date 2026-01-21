import Keycloak from 'keycloak-js';

const initOptions = {
  url: process.env.VUE_APP_IAM_URL ? process.env.VUE_APP_IAM_URL : `${window.location.origin}/iam`,
  realm: 'cytomine',
  clientId: 'core',
  enableLogging: true
};

const _keycloak = new Keycloak(initOptions);

const plugin = {
  install: Vue => {
    Vue.$keycloak = _keycloak;
  },
};
plugin.install = Vue => {
  Vue.$keycloak = _keycloak;
  Object.defineProperties(Vue.prototype, {
    $keycloak: {
      get() {
        return _keycloak;
      },
    },
  });
};

export default plugin;
