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

export function flattenMetadata(value, prefix = '') {
  let result = [];
  if (value === null || value === undefined || value === '') {
    return result;
  }

  if (Array.isArray(value)) {
    value.forEach((item, index) => {
      result.push(...flattenMetadata(item, prefix ? `${prefix}.${index}` : `${index}`));
    });
    return result;
  }

  if (typeof value === 'object') {
    for (let [key, inner] of Object.entries(value)) {
      result.push(...flattenMetadata(inner, prefix ? `${prefix}.${key}` : key));
    }
    return result;
  }

  result.push({ fullKey: prefix, value });
  return result;
}

export async function fetchImageMetadata(abstractImageId) {
  if (!abstractImageId) {
    return [];
  }
  try {
    let { data } = await Cytomine.instance.api.get('meilisearch/search', {
      params: {
        filter: `image.abstract_image_id = ${abstractImageId}`,
        limit: 1,
      }
    });

    if (!Array.isArray(data) || data.length === 0) {
      return [];
    }

    return flattenMetadata(data[0]);
  } catch (error) {
    console.error('Failed to fetch search metadata: ', error);
    return [];
  }
}
