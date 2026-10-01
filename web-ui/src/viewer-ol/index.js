/**
 * Registers the handful of `vue3-openlayers` components the viewer uses, plus
 * the Cytomine tile source that has no equivalent there.
 *
 * They are imported one by one rather than through `vue3-openlayers`' own
 * plugin: that one registers every component it ships, which would pull
 * `jspdf`, `proj4`, `ol-ext` and `ol-contextmenu` into the bundle for
 * components this app never renders.
 */
import OlMap from 'vue3-openlayers/map/OlMap';
import OlView from 'vue3-openlayers/map/OlView';
import OlTileLayer from 'vue3-openlayers/layers/OlTileLayer';
import OlVectorLayer from 'vue3-openlayers/layers/OlVectorLayer';
import OlSourceVector from 'vue3-openlayers/sources/OlSourceVector';
import OlInteractionDraw from 'vue3-openlayers/interactions/OlInteractionDraw';
import OlInteractionSelect from 'vue3-openlayers/interactions/OlInteractionSelect';

import OlSourceCytomine from './OlSourceCytomine.vue';

const components = {
  'ol-map': OlMap,
  'ol-view': OlView,
  'ol-tile-layer': OlTileLayer,
  'ol-vector-layer': OlVectorLayer,
  'ol-source-vector': OlSourceVector,
  'ol-source-cytomine': OlSourceCytomine,
  'ol-interaction-draw': OlInteractionDraw,
  'ol-interaction-select': OlInteractionSelect
};

export default {
  install(app) {
    Object.entries(components).forEach(([name, component]) => app.component(name, component));
  }
};

export { components };
