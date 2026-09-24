import { mount } from '@vue/test-utils';
import Collection from 'ol/Collection';
import Map from 'ol/Map';
import Select from 'ol/interaction/Select';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';

import OlMap from 'vue3-openlayers/map/OlMap';
import OlVectorLayer from 'vue3-openlayers/layers/OlVectorLayer';
import OlSourceVector from 'vue3-openlayers/sources/OlSourceVector';
import OlInteractionSelect from 'vue3-openlayers/interactions/OlInteractionSelect';

/**
 * The viewer reaches for `vue3-openlayers`' ol objects through `$refs` and its
 * `provide` keys, because none of the four things it needs (a custom tile
 * source, a `loader`/`strategy` vector source, an imperatively restyled layer,
 * and a rectangle-aware modify) can be expressed with props alone. None of that
 * is public API, so it is pinned here: if an upgrade renames an exposed
 * property or an inject key, this fails rather than the viewer silently going
 * blank.
 */
describe('vue3-openlayers contract', () => {
  it('should expose the ol layer and source, and provide the map', () => {
    const wrapper = mount({
      components: { OlMap, OlVectorLayer, OlSourceVector },
      template: `
        <ol-map ref="map">
          <ol-vector-layer ref="layer">
            <ol-source-vector ref="source" />
          </ol-vector-layer>
        </ol-map>`
    });

    expect(wrapper.vm.$refs.map.map).toBeInstanceOf(Map);
    expect(wrapper.vm.$refs.layer.vectorLayer).toBeInstanceOf(VectorLayer);
    expect(wrapper.vm.$refs.source.source).toBeInstanceOf(VectorSource);

    // ...and the source ends up on the layer, which is how the annotation layer
    // gets one at all.
    expect(wrapper.vm.$refs.layer.vectorLayer.getSource())
      .toBe(wrapper.vm.$refs.source.source);
  });

  it('should hand the vector source its loader and strategy', () => {
    const loader = vi.fn();
    const strategy = vi.fn(extent => [extent]);

    const wrapper = mount({
      components: { OlMap, OlVectorLayer, OlSourceVector },
      data: () => ({ loader, strategy }),
      template: `
        <ol-map>
          <ol-vector-layer>
            <ol-source-vector ref="source" :loader="loader" :strategy="strategy" />
          </ol-vector-layer>
        </ol-map>`
    });

    // vuelayers needed a `url="-"` hack to get its loader called at all; plain
    // ol takes both directly.
    const source = wrapper.vm.$refs.source.source;
    source.loadFeatures([0, 0, 10, 10], 1, source.getProjection());

    expect(strategy).toHaveBeenCalled();
    expect(loader).toHaveBeenCalled();
  });

  it('should expose the ol Select and its feature collection', () => {
    const wrapper = mount({
      components: { OlMap, OlInteractionSelect },
      template: '<ol-map><ol-interaction-select ref="select" :multi="true" /></ol-map>'
    });

    const select = wrapper.vm.$refs.select.select;
    expect(select).toBeInstanceOf(Select);
    // The edit interactions are driven off this collection, which is what
    // `select-target-<index>` used to name in vuelayers' identity map.
    expect(select.getFeatures()).toBeInstanceOf(Collection);
  });
});
