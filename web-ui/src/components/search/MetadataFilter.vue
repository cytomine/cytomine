<template>
  <div class="metadata-filter">
    <template v-if="facets.length">
      <h1>Metadata filters</h1>

      <b-field label="Search from metadata">
        <b-input
          :value="searchString"
          @input="debounceSearchString"
          icon="search"
          :placeholder="$t('search-placeholder')"
        />
      </b-field>

      <div class="facet-filters">
        <b-field v-for="item in facets" :key="item.key" :label="item.key">
          <cytomine-multiselect
            v-model="selectedFacets[item.key]"
            :options="item.values"
            :multiple="true"
            :allPlaceholder="$t('all')"
          />
        </b-field>
      </div>

      <b-button icon-left="times" @click="clear()">{{ $t('button-clear') }}</b-button>
    </template>
  </div>
</template>

<script lang="js">
import _ from 'lodash';

import CytomineMultiselect from '@/components/form/CytomineMultiselect.vue';
import { fetchFacets } from '@/utils/search';

export default {
  name: 'MetadataFilter',
  components: {
    CytomineMultiselect,
  },
  data() {
    return {
      facets: [],
      selectedFacets: {},
      searchString: '',
    };
  },
  computed: {
    filters() {
      return Object.entries(this.selectedFacets)
        .filter(([, values]) => values.length > 0)
        .map(([key, values]) => {
          // Keep filters that are composed of several words with commas
          let clause = values
            .map(value => `${key} = "${value.replace(/"/g, '\\"')}"`)
            .join(' OR ');
          return values.length === 1 ? clause : `(${clause})`;
        });
    },
  },
  watch: {
    filters() {
      this.emitFilterChange();
    },
    searchString() {
      this.emitFilterChange();
    },
  },
  methods: {
    async fetchFacets() {
      let facets = await fetchFacets();

      this.facets = Object.entries(facets).map(([key, inner]) => ({
        key,
        values: Object.keys(inner)
      }));
      this.selectedFacets = Object.fromEntries(this.facets.map(({ key }) => [key, []]));
    },
    emitFilterChange: _.debounce(function () {
      this.$emit('filter-change', {
        query: this.searchString,
        filters: this.filters,
      });
    }, 300),
    clear() {
      this.searchString = '';
      this.selectedFacets = Object.fromEntries(this.facets.map(({ key }) => [key, []]));
    },
    debounceSearchString: _.debounce(function (value) {
      this.searchString = value;
    }, 500),
  },
  created() {
    this.fetchFacets();
  },
};
</script>

<style>
.metadata-filter {
  margin: 1.5rem 0;
}

.metadata-filter h1 {
  margin-bottom: 1rem;
}

.facet-filters {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(30rem, 1fr));
  gap: 0.75rem 1rem;
  margin-bottom: 1rem;
}

.facet-filters .field {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin-bottom: 0;
  min-width: 0;
}

.facet-filters .label {
  overflow-wrap: anywhere;
}
</style>
