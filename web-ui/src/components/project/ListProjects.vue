<template>
<div class="list-projects-wrapper content-wrapper">
  <b-loading :is-full-page="false" :active="loading" />
  <div class="box error" v-if="error">
    <h2> {{ $t('error') }} </h2>
    <p>{{ $t('unexpected-error-info-message') }}</p>
  </div>
  <div v-else-if="!loading" class="panel">
    <p class="panel-heading">
      {{$t('projects')}}
      <button v-if="!currentUser.guestByNow" class="button is-link" @click="creationModal = true">
        {{$t('new-project')}}
      </button>
    </p>
    <div class="panel-block">
      <div class="search-block">
        <b-input
          class="search-projects"
          v-model="searchString"
          :placeholder="$t('search-placeholder')"
          type="search" icon="search"
        />
        <button class="button" @click="toggleFilterDisplay()">
          <span class="icon">
            <i class="fas fa-filter"></i>
          </span>
          <span>
            {{filtersOpened ? $t('button-hide-filters') : $t('button-show-filters')}}
          </span>
          <span v-if="nbActiveFilters" class="nb-active-filters">
            {{nbActiveFilters}}
          </span>
        </button>
      </div>

      <b-collapse :open="filtersOpened">
        <div class="filters">
          <div class="columns">
            <div class="column filter">
              <div class="filter-label">
                {{$t('ontology')}}
              </div>
              <div class="filter-body">
                <cytomine-multiselect v-model="selectedOntologies" :options="availableOntologies"
                  label="name" track-by="name" :multiple="true" :allPlaceholder="$t('all-ontologies')" />
              </div>
            </div>

            <div class="column filter">
              <div class="filter-label">
                {{$t('my-role')}}
              </div>
              <div class="filter-body">
                <cytomine-multiselect v-model="selectedRoles" :options="availableRoles"
                  label="name" track-by="name" :multiple="true" :searchable="false" />
              </div>
            </div>

            <div class="column filter">
              <div class="filter-label">
                {{$t('tags')}}
              </div>
              <div class="filter-body">
                <cytomine-multiselect v-model="selectedTags" :options="availableTags"
                  label="name" track-by="name" :multiple="true" :allPlaceholder="$t('all')" />
              </div>
            </div>
          </div>

          <div class="columns">
              <div class="column filter">
                <div class="filter-label">
                  {{$t('members')}}
                </div>
                <div class="filter-body">
                  <cytomine-slider v-model="boundsMembers" :max="maxNbMembers" />
                </div>
              </div>

              <div class="column filter">
                <div class="filter-label">
                  {{$t('images')}}
                </div>
                <div class="filter-body">
                  <cytomine-slider v-model="boundsImages" :max="maxNbImages" />
                </div>
              </div>

              <div class="column"></div>
          </div>

          <div class="columns">
            <div class="column filter">
              <div class="filter-label">
                {{$t('user-annotations')}}
              </div>
              <div class="filter-body">
                <cytomine-slider v-model="boundsUserAnnotations" :max="maxNbUserAnnotations" />
              </div>
            </div>

            <div class="column filter">
              <div class="filter-label">
                {{$t('reviewed-annotations')}}
              </div>
              <div class="filter-body">
                <cytomine-slider v-model="boundsReviewedAnnotations" :max="maxNbReviewedAnnotations" />
              </div>
            </div>
            <div class="column"></div>
          </div>
        </div>
      </b-collapse>

      <cytomine-table
        :collection="projectCollection"
        class="table-projects"
        :currentPage.sync="currentPage"
        :perPage.sync="perPage"
        :openedDetailed.sync="openedDetails"
        :sort.sync="sortField"
        :order.sync="sortOrder"
        :revision="revision"
      >
        <template #default="{row: project}">
          <b-table-column field="currentUserRole" label="" centered width="1">
            <icon-project-member-role
              :is-manager="project.currentUserRoles?.admin"
              :is-representative="project.currentUserRoles?.representative"
            />
          </b-table-column>

          <b-table-column field="name" :label="$t('name')" sortable width="250">
            <router-link :to="`/project/${project.id}`">
              {{ project.name }}
            </router-link>
          </b-table-column>

          <b-table-column field="members_count" :label="$t('members')" centered sortable width="150">
            {{ project.membersCount }}
          </b-table-column>

          <b-table-column field="count_images" :label="$t('images')" centered sortable width="150">
            <router-link :to="`/project/${project.id}/images`">{{ project.numberOfImages }}</router-link>
          </b-table-column>

          <b-table-column field="count_annotations" :label="$t('user-annotations')" centered sortable width="150">
            <router-link :to="`/project/${project.id}/annotations?type=user`">
              {{ project.numberOfAnnotations }}
            </router-link>
          </b-table-column>

          <b-table-column field="count_reviewed_annotations" :label="$t('reviewed-annotations')" centered sortable width="150">
            <router-link :to="`/project/${project.id}/annotations?type=reviewed`">
              {{ project.numberOfReviewedAnnotations }}
            </router-link>
          </b-table-column>

          <b-table-column field="last_activity" :label="$t('last-activity')" centered sortable width="180">
            <template v-if="project.lastActivity">
              {{ formatMomentDate(Number(project.lastActivity), 'll') }}
            </template>
          </b-table-column>

          <b-table-column label=" " centered width="150">
            <router-link :to="`/project/${project.id}`" class="button is-small is-link">
              {{$t('button-open')}}
            </router-link>
          </b-table-column>
        </template>

        <template #detail="{row: project}">
          <project-details
            :project="project"
            :excluded-properties="excludedProperties"
            editable
            @update="updateProject()"
            @delete="deleteProject(project)"
          />
        </template>

        <template #empty>
          <div class="content has-text-grey has-text-centered">
            <p>{{$t('no-project')}}</p>
          </div>
        </template>
      </cytomine-table>

      <div class="legend">
          <h2>{{$t('legend')}}</h2>
          <p><icon-project-member-role /> : {{$t('contributor-icon-label')}}</p>
          <p><icon-project-member-role :is-manager="true" /> : {{$t('manager-icon-label')}}</p>
          <p><icon-project-member-role :is-manager="true" :is-representative="true" /> : {{$t('representative-icon-label')}}</p>
      </div>
    </div>
  </div>

  <add-project-modal :active.sync="creationModal" :ontologies="ontologies" />
</div>
</template>

<script>
import CytomineTable from '@/components/utils/CytomineTable.vue';
import CytomineMultiselect from '@/components/form/CytomineMultiselect.vue';
import CytomineSlider from '@/components/form/CytomineSlider.vue';
import ProjectDetails from './ProjectDetails.vue';
import AddProjectModal from './AddProjectModal.vue';
import IconProjectMemberRole from '@/components/icons/IconProjectMemberRole.vue';

import { get, sync, syncBoundsFilter, syncMultiselectFilter } from '@/utils/store-helpers';
import { formatMomentDate } from '@/utils/date';

import { ProjectCollection, OntologyCollection } from '@/api';

export default {
  name: 'list-projects',
  components: {
    CytomineTable,
    CytomineMultiselect,
    CytomineSlider,
    ProjectDetails,
    AddProjectModal,
    IconProjectMemberRole
  },
  data() {
    return {
      loading: true,
      error: false,

      ontologies: [],

      creationModal: false,

      availableFilters: {},
      maxNbMembers: 10,
      maxNbImages: 10,
      maxNbUserAnnotations: 100,
      maxNbReviewedAnnotations: 100,

      excludedProperties: [
        'name',
        'membersCount',
        'numberOfImages',
        'numberOfAnnotations',
        'numberOfReviewedAnnotations',
        'lastActivity'
      ],

      revision: 0
    };
  },
  computed: {
    currentUser: get('currentUser/user'),

    searchString: sync('listProjects/searchString', { debounce: 500 }),
    filtersOpened: sync('listProjects/filtersOpened'),
    nbActiveFilters() {
      return this.$store.getters['listProjects/nbActiveFilters'];
    },

    availableRoles() {
      return (this.availableFilters.roleNames ?? []).map(role => ({
        name: role === 'admin' ? this.$t('manager') : this.$t('contributor'),
        value: role
      })).sort((a, b) => a.name.localeCompare(b.name));
    },
    availableOntologies() {
      return (this.availableFilters.ontologyNames ?? []).map(name => ({ name }))
        .sort((a, b) => a.name.localeCompare(b.name));
    },
    availableTags() {
      return (this.availableFilters.tagLabels ?? []).map(name => ({ name }))
        .sort((a, b) => a.name.localeCompare(b.name));
    },

    selectedOntologies: syncMultiselectFilter('listProjects', 'selectedOntologies', 'availableOntologies'),
    selectedRoles: syncMultiselectFilter('listProjects', 'selectedRoles', 'availableRoles'),
    selectedTags: syncMultiselectFilter('listProjects', 'selectedTags', 'availableTags'),

    boundsMembers: syncBoundsFilter('listProjects', 'boundsMembers', 'maxNbMembers'),
    boundsImages: syncBoundsFilter('listProjects', 'boundsImages', 'maxNbImages'),
    boundsUserAnnotations: syncBoundsFilter('listProjects', 'boundsUserAnnotations', 'maxNbUserAnnotations'),
    boundsReviewedAnnotations: syncBoundsFilter('listProjects', 'boundsReviewedAnnotations', 'maxNbReviewedAnnotations'),

    projectCollection() {
      let collection = new ProjectCollection();
      let filters = {};
      if (this.selectedOntologies.length > 0 && this.selectedOntologies.length < this.availableOntologies.length) {
        filters.ontologyNames = this.selectedOntologies.map(ontology => ontology.name);
      }
      if (this.selectedRoles.length > 0 && this.selectedRoles.length < this.availableRoles.length) {
        filters.roleNames = this.selectedRoles.map(role => role.value);
      }
      if (this.searchString) {
        filters.name = this.searchString;
      }
      if (this.selectedTags.length > 0 && this.selectedTags.length < this.availableTags.length) {
        filters.tagLabels = this.selectedTags.map(tag => tag.name);
      }
      [
        { key: 'memberCount', bounds: this.boundsMembers, max: this.maxNbMembers },
        { key: 'imageCount', bounds: this.boundsImages, max: this.maxNbImages },
        { key: 'manualAnnotationCount', bounds: this.boundsUserAnnotations, max: this.maxNbUserAnnotations },
        { key: 'reviewedAnnotationCount', bounds: this.boundsReviewedAnnotations, max: this.maxNbReviewedAnnotations }
      ].forEach(({ key, bounds, max }) => {
        if (bounds[0] > 0 || bounds[1] < max) {
          filters[key] = {};
          if (bounds[0] > 0) {
            filters[key].min = bounds[0];
          }
          if (bounds[1] !== max) {
            filters[key].max = bounds[1];
          }
        }
      });
      if (Object.keys(filters).length > 0) {
        collection.filters = filters;
      }
      return collection;
    },
    currentPage: sync('listProjects/currentPage'),
    perPage: sync('listProjects/perPage'),
    sortField: sync('listProjects/sortField'),
    sortOrder: sync('listProjects/sortOrder'),
    openedDetails: sync('listProjects/openedDetails')
  },
  watch: {
    revision() {
      this.fetchOntologies();
      this.fetchAvailableFilters();
    }
  },
  methods: {
    formatMomentDate,
    toggleFilterDisplay() {
      this.filtersOpened = !this.filtersOpened;
    },
    async fetchOntologies() {
      let ontologies = (await OntologyCollection.fetchAll({ light: true })).array;
      ontologies.sort((a, b) => a.name.localeCompare(b.name));
      this.ontologies = ontologies;
    },
    async fetchAvailableFilters() {
      this.availableFilters = (await ProjectCollection.fetchAvailableFilters()) ?? {};
      this.maxNbMembers = Math.max(10, this.availableFilters.memberCount?.max ?? 0);
      this.maxNbImages = Math.max(10, this.availableFilters.imageCount?.max ?? 0);
      this.maxNbUserAnnotations = Math.max(100, this.availableFilters.manualAnnotationCount?.max ?? 0);
      this.maxNbReviewedAnnotations = Math.max(100, this.availableFilters.reviewedAnnotationCount?.max ?? 0);
    },
    updateProject() {
      this.revision++;
    },
    async deleteProject(projectToDelete) {
      try {
        await projectToDelete.delete();
        this.revision++;
        this.$notify({
          type: 'success',
          text: this.$t('notif-success-project-deletion', { projectName: projectToDelete.name })
        });
      } catch (error) {
        this.$notify({
          type: 'error',
          text: this.$t('notif-error-project-deletion', { projectName: projectToDelete.name })
        });
        return;
      }
    }
  },
  async created() {
    try {
      await Promise.all([
        this.fetchOntologies(),
        this.fetchAvailableFilters()
      ]);
    } catch (error) {
      console.log(error);
      this.error = true;
    }

    this.loading = false;
  }
};
</script>

<style scoped>
.panel-block {
  padding-top: 0.8em;
}

.panel-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-block {
  display: flex;
}

.legend {
  margin-top: 0.8rem;
  margin-bottom: 1rem;
  border-radius: 10px;
  padding: 1rem 1.5em;
  background: #f8f8f8;
}

.legend p:not(:last-child) {
  margin-bottom: 0.4em;
}
</style>

<style>
.search-projects {
  max-width: 25em;
  margin-right: 1em;
}

.table-projects {
  margin-top: 1rem;
}

.list-projects-wrapper td, .list-projects-wrapper th {
  vertical-align: middle !important;
}
</style>
