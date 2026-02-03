import Keycloak from 'keycloak-js';
import constants from './utils/constants';

const initOptions = {
  url: constants.IAM_URL ? constants.IAM_URL : `${window.location.origin}/iam`,
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
