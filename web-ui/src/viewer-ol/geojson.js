/**
 * Replaces `createGeoJsonFmt` from `vuelayers/lib/ol-ext/format`.
 *
 * The store keeps selected annotations as GeoJSON objects rather than as ol
 * features, and the viewer relies on two details of the vuelayers subclass that
 * plain `ol/format/GeoJSON` does not provide:
 *
 * - `properties` is `null` (not omitted) for a feature that carries no
 *   properties. `SelectInteraction` tells an annotation apart from a dragged
 *   vertex with `feature.properties === null`.
 * - a `Circle` geometry is written out as a circular polygon instead of
 *   throwing, because GeoJSON has no circle. vuelayers reprojected it to
 *   EPSG:4326 on the way; that is meaningless in the per-image pixel
 *   projection, so the polygon is kept in the feature projection here.
 */
import GeoJSONFormat from 'ol/format/GeoJSON';
import { fromCircle } from 'ol/geom/Polygon';

const CIRCLE_SIDES = 32;

export class GeoJSON extends GeoJSONFormat {
  writeGeometryObject(geometry, options) {
    if (geometry.getType() === 'Circle') {
      geometry = fromCircle(geometry, CIRCLE_SIDES);
    }

    return super.writeGeometryObject(geometry, options);
  }

  writeFeatureObject(feature, options) {
    const object = { type: 'Feature' };

    const id = feature.getId();
    if (id !== undefined) {
      object.id = id;
    }

    const geometry = feature.getGeometry();
    object.geometry = geometry ? this.writeGeometryObject(geometry, options) : null;

    const properties = feature.getProperties();
    delete properties[feature.getGeometryName()];
    object.properties = Object.keys(properties).length > 0 ? properties : null;

    return object;
  }
}

export function createGeoJsonFmt(options) {
  return new GeoJSON(options);
}
