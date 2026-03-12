import Keycloak from 'keycloak-js';
import constants from './utils/constants';

let _keycloak = null;

function getKeycloak() {
  if (!_keycloak) {
    const initOptions = {
      url: constants.IAM_URL,
      realm: 'cytomine',
      clientId: 'core',
      enableLogging: true
    };
    _keycloak = new Keycloak(initOptions);
  }
  return _keycloak;
}

const plugin = {
  install: Vue => {
    Object.defineProperty(Vue, '$keycloak', {
      get() {
        return getKeycloak();
      }
    });
    Object.defineProperties(Vue.prototype, {
      $keycloak: {
        get() {
          return getKeycloak();
        },
      },
    });
  },
};

export default plugin;
