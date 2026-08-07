import { Cytomine } from '@/api';

export async function fetchFacets() {
  try {
    let { data } = await Cytomine.instance.api.get('meilisearch/facets');
    return data;
  } catch (error) {
    console.error('Failed to fetch facets: ', error);
    return {};
  }
}

export async function searchMetadata({ query = '', filters = [], limit = 20, offset = 0 } = {}) {
  let params = new URLSearchParams();
  params.append('query', query);
  filters.forEach(filter => params.append('filter', filter));
  params.append('limit', limit);
  params.append('offset', offset);

  try {
    let { data } = await Cytomine.instance.api.get('meilisearch/search', { params });
    return data;
  } catch (error) {
    console.error('Failed to search metadata: ', error);
    return [];
  }
}
