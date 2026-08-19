/**
 * Replaces `src/vuelayers-suppl/cytomine-source/`, which was a vuelayers
 * `tile-source` mixin. Everything here is plain OpenLayers, so it is testable
 * without mounting a component.
 *
 * PIMS serves normalized tiles under `.../zoom/{z}/tx/{x}/ty/{y}.jpg`, where
 * `ty` is `TileY` with `ge=0`, documented as "0 is top"
 * (`pims/pims/api/utils/models.py`).
 */
import TileImage from 'ol/source/TileImage';
import TileGrid from 'ol/tilegrid/TileGrid';
import { CustomTile } from 'ol/source/Zoomify';
import { toSize } from 'ol/size';

/**
 * Resolutions from the coarsest (2^nbResolutions) down to 1 pixel per pixel,
 * the order `ol/tilegrid/TileGrid` expects.
 */
export function createResolutions(nbResolutions) {
  const resolutions = [1];
  for (let i = 1; i <= nbResolutions; ++i) {
    resolutions.push(1 << i);
  }
  return resolutions.reverse();
}

export function createTileGrid({ extent, tileSize, nbResolutions }) {
  return new TileGrid({
    tileSize: toSize(tileSize),
    extent,
    origin: [extent[0], extent[3]],
    resolutions: createResolutions(nbResolutions)
  });
}

/**
 * `{y}` is used verbatim, *not* flipped.
 *
 * vuelayers routed tile URLs through `ol-tilecache@3`, which substituted
 * `-tileCoord[2] - 1` for `{y}`. That flip existed because ol 5 numbered rows
 * upwards from a bottom origin, so a top-left origin like this one produced
 * negative row indices. Since ol 6 `TileGrid` measures rows downwards from the
 * origin (`(origin[1] - y) / resolution / tileSize[1]`), so `tileCoord[2]` is
 * already `0` at the top. Carrying the flip over would request `ty=-1` at every
 * zoom level and load no tiles at all.
 */
export function createTileUrlFunction(template) {
  return tileCoord => {
    if (!tileCoord) {
      return undefined;
    }

    return template
      .replace(/{z}/g, String(tileCoord[0]))
      .replace(/{x}/g, String(tileCoord[1]))
      .replace(/{y}/g, String(tileCoord[2]));
  };
}

export default class CytomineTileSource extends TileImage {
  constructor({
    url,
    extent,
    tileSize,
    nbResolutions,
    projection,
    tileLoadFunction,
    crossOrigin,
    transition
  }) {
    const size = toSize(tileSize);
    const tileGrid = createTileGrid({ extent, tileSize: size, nbResolutions });

    super({
      projection,
      tileGrid,
      tileLoadFunction,
      crossOrigin,
      transition,
      // Since ol 6 `CustomTile` takes the tile *size*; ol 5 took the tile grid
      // and derived the size per zoom level. Equivalent here because this grid
      // uses a single tile size for every level.
      tileClass: CustomTile.bind(null, size),
      tileUrlFunction: createTileUrlFunction(url)
    });
  }

  /**
   * Not named `setUrl`: `ol/source/UrlTile#setUrl` builds its own tile URL
   * function from the template and would drop the `{y}` handling above.
   */
  setUrlTemplate(url) {
    this.setTileUrlFunction(createTileUrlFunction(url));
  }
}
