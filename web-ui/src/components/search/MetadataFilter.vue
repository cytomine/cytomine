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

      <div v-if="searched" class="search-results">
        <h2>{{ results.length }} result(s)</h2>

        <p v-if="!results.length">No result</p>
        <ul v-else>
          <li v-for="result in results" :key="result.id">
            {{ resultLabel(result) }}
          </li>
        </ul>
      </div>
    </template>
  </div>
</template>

<script lang="js">
import _ from 'lodash';

import CytomineMultiselect from '@/components/form/CytomineMultiselect.vue';
import { fetchFacets, searchMetadata } from '@/utils/search';

export default {
  name: 'MetadataFilter',
  components: {
    CytomineMultiselect,
  },
  props: {
    limit: { type: Number, default: 20 },
    offset: { type: Number, default: 0 },
  },
  data() {
    return {
      facets: [],
      selectedFacets: {},
      searchString: '',
      results: [],
      searched: false,
    };
  },
  computed: {
    filters() {
      return Object.entries(this.selectedFacets)
        .filter(([, values]) => values.length > 0)
        .map(([key, values]) => {
          if (values.length === 1) {
            return `${key}:${values[0]}`;
          }
          return `(${values.map(value => `${key} = "${value}"`).join(' OR ')})`;
        });
    },
  },
  watch: {
    filters() {
      this.search();
    },
    searchString() {
      this.search();
    },
    limit() {
      this.search();
    },
    offset() {
      this.search();
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
    search: _.debounce(async function () {
      this.results = await searchMetadata({
        query: this.searchString,
        filters: this.filters,
        limit: this.limit,
        offset: this.offset,
      });
      this.searched = true;
      this.$emit('search', this.results);
    }, 300),
    resultLabel(result) {
      let image = result.image || {};
      return image.name || image.identifier || result.id;
    },
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

.metadata-filter .search-results {
  margin-top: 1rem;
}

.metadata-filter .search-results ul {
  list-style: disc inside;
}
</style>
