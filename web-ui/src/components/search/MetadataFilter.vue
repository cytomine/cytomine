<template>
  <div>
    <h1>Metadata filters</h1>

    <b-field label="Search from metadata">
      <b-input icon="search" :placeholder="$t('search-placeholder')"/>
    </b-field>

    <div>Free text search</div>

    <div>Biological species</div>

    <div>Anatomical site</div>

    <div>Diagnosis</div>

    <div>Staining</div>

    <b-button icon-left="times">{{ $t('button-clear') }}</b-button>

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
