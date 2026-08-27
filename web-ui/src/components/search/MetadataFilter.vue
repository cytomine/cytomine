<template>
  <div class="metadata-filter">
    <template v-if="facets.length">
      <h1>Metadata filters</h1>

      <b-field label="Search from metadata">
        <b-input
          :model-value="searchString"
          @update:model-value="debounceSearchString"
          icon="search"
          :placeholder="$t('search-placeholder')"
        />
      </b-field>

      <div class="facet-filters">
        <b-field v-for="item in facets" :key="item.key" :label="facetLabel(item.key)">
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

const FACET_LABELS = {
  'specimens.biological_being.animal_species.meaning': 'Animal species',
  'specimens.anatomical_site.meaning': 'Anatomical site',
  'specimens.biological_being.sex': 'Sex',
  'specimens.age_at_extraction.interval_start': 'Age at extraction',
  'slide.staining.stains.compound.meaning': 'Stain compound',
  'specimens.fixation_type.meaning': 'Fixation type',
  'block.block_preparation.meaning': 'Block preparation',
  'specimens.specimen_type.meaning': 'Specimen type',
};

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
    facetLabel(key) {
      if (FACET_LABELS[key]) {
        return FACET_LABELS[key];
      }
      let segment = key.replace(/\.meaning$/, '').split('.').pop();
      return segment.replace(/_/g, ' ').replace(/^\w/, c => c.toUpperCase());
    },
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
  justify-content: flex-start;
  align-self: start;
  margin-bottom: 0;
  min-width: 0;
}

.facet-filters .label {
  overflow-wrap: anywhere;
}
</style>
