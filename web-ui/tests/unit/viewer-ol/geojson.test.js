import Feature from 'ol/Feature';
import Circle from 'ol/geom/Circle';
import Point from 'ol/geom/Point';
import Polygon from 'ol/geom/Polygon';

import { createGeoJsonFmt } from '@/viewer-ol/geojson.js';

describe('viewer-ol/geojson', () => {
  const format = createGeoJsonFmt();

  it('should keep the annotation properties under `properties`', () => {
    const feature = new Feature(new Polygon([[[0, 0], [1, 0], [1, 1], [0, 0]]]));
    feature.setId(42);
    feature.set('annot', { id: 42, area: 3 });

    const written = format.writeFeatureObject(feature);

    expect(written.id).toBe(42);
    expect(written.properties.annot).toEqual({ id: 42, area: 3 });
  });

  it('should write `properties: null` rather than omitting it', () => {
    // `SelectInteraction` tells an annotation apart from a dragged vertex with
    // `feature.properties === null`, which plain ol/format/GeoJSON never emits.
    const written = format.writeFeatureObject(new Feature(new Point([1, 2])));

    expect(written.properties).toBeNull();
  });

  it('should leave out the id of a feature that has none', () => {
    const written = format.writeFeatureObject(new Feature(new Point([1, 2])));

    expect(Object.keys(written)).not.toContain('id');
  });

  it('should write a circle out as a polygon, since GeoJSON has no circle', () => {
    const feature = new Feature(new Circle([0, 0], 10));

    const written = format.writeFeatureObject(feature);

    expect(written.geometry.type).toBe('Polygon');
  });
});
