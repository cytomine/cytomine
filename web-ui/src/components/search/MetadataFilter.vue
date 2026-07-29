<template>
  <div>
    <h1>Filters</h1>

    <div>{{ facets }}</div>
  </div>
</template>

<script lang="js">
import { fetchFacets } from '@/utils/search';

export default {
  name: 'MetadataFilter',
  data() {
    return {
      facets: {},
    };
  },
  methods: {
    async fetchFacets() {
      this.facets = await fetchFacets() || {};
    },
  },
  created() {
    this.fetchFacets();
  },
};
</script>
