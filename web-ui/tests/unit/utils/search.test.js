import { Cytomine } from '@/api';
import { flattenMetadata, fetchImageMetadata } from '@/utils/search';

vi.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: vi.fn(),
      }
    }
  }
}));

describe('flattenMetadata()', () => {
  it('should flatten a nested object into dot-path keys', () => {
    const result = flattenMetadata({
      slide: { staining: { procedure: { meaning: 'H&E' } } }
    });

    expect(result).toEqual([
      { fullKey: 'slide.staining.procedure.meaning', value: 'H&E' }
    ]);
  });

  it('should index array elements', () => {
    const result = flattenMetadata({
      specimens: [
        { 'anatomical_site': { meaning: 'liver' } },
        { 'anatomical_site': { meaning: 'lung' } },
      ]
    });

    expect(result).toEqual([
      { fullKey: 'specimens.0.anatomical_site.meaning', value: 'liver' },
      { fullKey: 'specimens.1.anatomical_site.meaning', value: 'lung' },
    ]);
  });

  it('should keep primitive values including false and 0', () => {
    const result = flattenMetadata({
      image: { 'tma_status': false, 'color_depth': 0 }
    });

    expect(result).toEqual([
      { fullKey: 'image.tma_status', value: false },
      { fullKey: 'image.color_depth', value: 0 },
    ]);
  });

  it('should skip null, undefined, and empty-string values', () => {
    const result = flattenMetadata({
      a: null,
      b: undefined,
      c: '',
      d: 'kept',
    });

    expect(result).toEqual([{ fullKey: 'd', value: 'kept' }]);
  });

  it('should return an empty array for a null document', () => {
    expect(flattenMetadata(null)).toEqual([]);
  });
});

describe('fetchImageMetadata()', () => {
  beforeEach(() => {
    Cytomine.instance.api.get.mockReset();
  });

  it('should return an empty array and not call the API when id is falsy', async () => {
    const result = await fetchImageMetadata(undefined);

    expect(result).toEqual([]);
    expect(Cytomine.instance.api.get).not.toHaveBeenCalled();
  });

  it('should query meilisearch filtered by abstract image id and flatten the hit', async () => {
    Cytomine.instance.api.get.mockResolvedValue({
      data: [{ slide: { identifier: 'S1' } }]
    });

    const result = await fetchImageMetadata(42);

    expect(Cytomine.instance.api.get).toHaveBeenCalledWith('meilisearch/search', {
      params: { filter: 'image.abstract_image_id = 42', limit: 1 }
    });
    expect(result).toEqual([{ fullKey: 'slide.identifier', value: 'S1' }]);
  });

  it('should return an empty array when no document matches', async () => {
    Cytomine.instance.api.get.mockResolvedValue({ data: [] });

    expect(await fetchImageMetadata(42)).toEqual([]);
  });

  it('should return an empty array when the request fails', async () => {
    Cytomine.instance.api.get.mockRejectedValue(new Error('boom'));

    expect(await fetchImageMetadata(42)).toEqual([]);
  });
});
