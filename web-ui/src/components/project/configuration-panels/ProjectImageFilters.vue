<template>
<div class="project-image-filters-wrapper">
  <b-loading :is-full-page="false" :active="loading" />
  <b-message v-if="error" type="is-danger" has-icon icon-size="is-small">
    <h2> {{ $t('error') }} </h2>
    <p> {{ $t('unexpected-error-info-message') }} </p>
  </b-message>
  <template v-else-if="!loading">
    <b-input class="search-field" v-model="searchString" :placeholder="$t('search-placeholder')" type="search" icon="search" />

    <b-table
      :data="filteredImageFilters"
      default-sort="selected"
      default-sort-direction="desc"
      :paginated="true"
      :per-page="perPage"
      pagination-size="is-small"
    >

      <template #default="{row: filter}">
        <b-table-column field="name" :label="$t('name')" sortable>
          {{filter.name}}
        </b-table-column>

        <b-table-column field="selected" :label="$t('status')" sortable>
          <button :class="['button', filter.selected ? 'is-success' : 'is-danger']" @click="toggleFilter(filter)">
            {{$t(filter.selected ? 'enabled' : 'disabled')}}
          </button>
        </b-table-column>
      </template>

      <template #empty>
        <div class="content has-text-grey has-text-centered">
          <p>{{$t('no-image-filter')}}</p>
        </div>
      </template>

      <template #bottom-left>
        <b-select v-model="perPage" size="is-small">
          <option value="10">{{$t('count-per-page', {count: 10})}}</option>
          <option value="25">{{$t('count-per-page', {count: 25})}}</option>
          <option value="50">{{$t('count-per-page', {count: 50})}}</option>
          <option value="100">{{$t('count-per-page', {count: 100})}}</option>
        </b-select>
      </template>
    </b-table>
  </template>
</div>
</template>

<script>
import { get } from '@/utils/store-helpers';
import { ImageFilterCollection, ImageFilterProject, ImageFilterProjectCollection } from '@/api';
import { getWildcardRegexp } from '@/utils/string-utils';

export default {
  name: 'project-image-filters',
  data() {
    return {
      loading: true,
      error: false,

      searchString: '',
      perPage: 10,

      imageFilters: []
    };
  },
  computed: {
    project: get('currentProject/project'),

    filteredImageFilters() {
      let regexp = getWildcardRegexp(this.searchString);
      return this.imageFilters.filter(imgFilter => regexp.test(imgFilter.name));
    }
  },
  methods: {
    async toggleFilter(filter) {
      try {
        if (filter.selected) {
          await filter.imageFilterProject.delete();
          filter.selected = false;
        } else {
          let filterProject = await new ImageFilterProject({
            imageFilter: filter.id,
            project: this.project.id
          }).save();
          filter.imageFilterProject = filterProject;
          filter.selected = true;
        }
      } catch (error) {
        console.log(error);
        this.$notify({
          type: 'error',
          text: this.$t('notif-error-change-status-image-filter-project', { filterName: filter.name })
        });
      }
    }
  },
  async created() {
    try {
      let promiseFilters = ImageFilterCollection.fetchAll();
      let promiseFiltersProjects = ImageFilterProjectCollection.fetchAll({
        filterKey: 'project',
        filterValue: this.project.id
      });

      let filters = (await promiseFilters).array.filter(filter => filter.available);
      let filtersProject = (await promiseFiltersProjects).array;

      filters.forEach(filter => {
        filter.imageFilterProject = filtersProject.find(fp => fp.imageFilter === filter.id);
        filter.selected = (filter.imageFilterProject !== undefined);
      });

      this.imageFilters = filters;
    } catch (error) {
      console.log(error);
      this.error = true;
    }

    this.loading = false;
  }
};
</script>

<style scoped>
.project-image-filters-wrapper {
  min-height: 40vh;
}
</style>

<style>
.project-image-filters-wrapper .search-field {
  max-width: 25em;
}

.project-image-filters-wrapper .table {
  margin-top: 1.5em;
}

.project-image-filters-wrapper .table .button {
  padding: 0 3em;
}
</style>
