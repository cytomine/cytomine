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
