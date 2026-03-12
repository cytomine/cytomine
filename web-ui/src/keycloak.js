import Keycloak from 'keycloak-js';
import constants from './utils/constants';

let _keycloak = null;

function getKeycloak() {
  if (!_keycloak) {
    // Strip /realms/cytomine if present, as Keycloak JS adds it automatically
    let url = constants.IAM_URL;
    if (url.endsWith('/')) {
      url = url.slice(0, -1);
    }
    if (url.endsWith('/realms/cytomine')) {
      url = url.slice(0, -'/realms/cytomine'.length);
    }
    const initOptions = {
      url: url,
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
