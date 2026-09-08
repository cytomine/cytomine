import { Cytomine } from '@/api';

export async function fetchFacets(project) {
  try {
    let { data } = await Cytomine.instance.api.get('meilisearch/facets', {
      params: { project }
    });
    return data;
  } catch (error) {
    console.error('Failed to fetch facets: ', error);
    return {};
  }
}
