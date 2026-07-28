<template>
  <div>
    <h1>Filters</h1>

    <div>{{ facets }}</div>
  </div>
</template>

<script lang="js">

import {Cytomine} from '@/api';

export default {
  name: 'MetadataFilter',
  data() {
    return {
      facets: {},
    };
  },
  methods: {
    async fetchFacets() {
      try {
        let {data} = await Cytomine.instance.api.get('meilisearch/facets');
        this.facets = data || {};
      } catch (error) {
        console.log(error);
        this.error = true;
      }
    },
  },
  created() {
    this.fetchFacets();
  },
};
</script>
