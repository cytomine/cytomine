import CytomineTileSource, {
  createResolutions,
  createTileGrid,
  createTileUrlFunction
} from '@/viewer-ol/cytomine-tile-source.js';

describe('cytomine-tile-source', () => {
  const extent = [0, 0, 1000, 600];

  describe('createResolutions', () => {
    it('should go from the coarsest level down to one pixel per pixel', () => {
      expect(createResolutions(3)).toEqual([8, 4, 2, 1]);
    });

    it('should be a single level when the image has no pyramid', () => {
      expect(createResolutions(0)).toEqual([1]);
    });
  });

  describe('createTileGrid', () => {
    const tileGrid = createTileGrid({ extent, tileSize: 256, nbResolutions: 3 });

    it('should put the origin at the top left corner', () => {
      expect(tileGrid.getOrigin(0)).toEqual([0, 600]);
    });

    it('should number rows downwards from the top, which is what PIMS expects', () => {
      // ol 5 numbered rows upwards, so a top-left origin produced negative
      // indices and `ol-tilecache` flipped them back. Since ol 6 the top row is
      // already 0 - see `createTileUrlFunction`.
      expect(tileGrid.getTileCoordForCoordAndZ([0, 600], 0)).toEqual([0, 0, 0]);
      expect(tileGrid.getTileCoordForCoordAndZ([0, 0], 3)[2]).toBeGreaterThan(0);
    });
  });

  describe('createTileUrlFunction', () => {
    const template = 'https://host/sliceinstance/1/normalized-tile/zoom/{z}/tx/{x}/ty/{y}.jpg?t=1';
    const tileUrlFunction = createTileUrlFunction(template);

    it('should substitute the tile coordinate as is', () => {
      expect(tileUrlFunction([2, 3, 4]))
        .toBe('https://host/sliceinstance/1/normalized-tile/zoom/2/tx/3/ty/4.jpg?t=1');
    });

    it('should not flip the row index the way ol-tilecache did', () => {
      // The flip would have made this `ty/-1`, which loads no tile at all.
      expect(tileUrlFunction([0, 0, 0])).toContain('/ty/0.jpg');
    });

    it('should return nothing outside the grid', () => {
      expect(tileUrlFunction(null)).toBeUndefined();
    });
  });

  describe('CytomineTileSource', () => {
    const source = new CytomineTileSource({
      url: 'https://host/{z}/{x}/{y}.jpg',
      extent,
      tileSize: 256,
      nbResolutions: 3,
      projection: 'EPSG:3857'
    });

    it('should build its tile grid from the image extent', () => {
      expect(source.getTileGrid().getResolutions()).toEqual([8, 4, 2, 1]);
    });

    it('should rebuild the url function when the url changes', () => {
      source.setUrlTemplate('https://other/{z}/{x}/{y}.png');

      expect(source.getTileUrlFunction()([1, 2, 3])).toBe('https://other/1/2/3.png');
    });
  });
});
