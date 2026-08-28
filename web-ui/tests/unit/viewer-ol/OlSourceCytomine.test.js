import { mount } from '@vue/test-utils';
import OlTileLayer from 'vue3-openlayers/layers/OlTileLayer';

import CytomineTileSource from '@/viewer-ol/cytomine-tile-source.js';
import OlSourceCytomine from '@/viewer-ol/OlSourceCytomine.vue';

/**
 * `vue3-openlayers` ships no component for a custom `TileImage`, so this one
 * has to hook into `<ol-tile-layer>` through the `tileLayer` key it provides.
 * That contract is not public API, so it is worth a test.
 */
describe('OlSourceCytomine.vue', () => {
  const props = {
    url: 'https://host/{z}/{x}/{y}.jpg',
    extent: [0, 0, 1000, 600],
    tileSize: [256, 256],
    nbResolutions: 3,
    projection: 'EPSG:3857'
  };

  const mountInLayer = () => mount({
    components: { OlTileLayer, OlSourceCytomine },
    data: () => ({ props }),
    template: '<ol-tile-layer ref="layer"><ol-source-cytomine ref="source" v-bind="props" /></ol-tile-layer>'
  });

  it('should install its source on the tile layer above it', () => {
    const wrapper = mountInLayer();

    const layer = wrapper.vm.$refs.layer.tileLayer;
    expect(layer.getSource()).toBeInstanceOf(CytomineTileSource);
  });

  it('should take the source off the layer when it goes away', () => {
    const wrapper = mountInLayer();
    const layer = wrapper.vm.$refs.layer.tileLayer;

    wrapper.unmount();

    expect(layer.getSource()).toBeNull();
  });

  it('should refresh the tiles when the url changes', async () => {
    const wrapper = mountInLayer();
    const source = wrapper.vm.$refs.source.source;

    await wrapper.setData({ props: { ...props, url: 'https://other/{z}/{x}/{y}.png' } });

    expect(source.getTileUrlFunction()([1, 2, 3])).toBe('https://other/1/2/3.png');
  });
});
