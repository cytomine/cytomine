/**
 * Replaces `createProj` / `addProj` / `getProj` from `vuelayers/lib/ol-ext`.
 *
 * Cytomine images are displayed in a per-image pixel projection (`CYTO-<id>`),
 * which is a plain `ol/proj/Projection` — no proj4 definition is involved, so
 * `vue3-openlayers`' `<ol-projection-register>` (which is a proj4 wrapper) does
 * not apply here.
 */
import Projection from 'ol/proj/Projection';
import { addProjection, get as getProjection } from 'ol/proj';

export function createProj(options) {
  return new Projection(options);
}

export function addProj(projection) {
  return addProjection(projection);
}

export function getProj(projectionLike) {
  return getProjection(projectionLike);
}
