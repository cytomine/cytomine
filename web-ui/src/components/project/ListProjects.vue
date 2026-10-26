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
                  label="name" track-by="id" :multiple="true" :allPlaceholder="$t('all-ontologies')" />
              </div>
            </div>

            <div class="column filter">
              <div class="filter-label">
                {{$t('my-role')}}
              </div>
              <div class="filter-body">
                <cytomine-multiselect v-model="selectedRoles" :options="availableRoles" multiple :searchable="false" />
              </div>
            </div>

            <div class="column filter">
              <div class="filter-label">
                {{$t('tags')}}
              </div>
              <div class="filter-body">
                <cytomine-multiselect v-model="selectedTags" :options="availableTags"
                  label="name" track-by="id" :multiple="true" :allPlaceholder="$t('all')" />
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
import ProjectDetails from './ProjectDetails.vue';
import AddProjectModal from './AddProjectModal.vue';
import IconProjectMemberRole from '@/components/icons/IconProjectMemberRole.vue';

import { get, sync } from '@/utils/store-helpers';
import { formatMomentDate } from '@/utils/date';

import { ProjectCollection, OntologyCollection } from '@/api';

export default {
  name: 'list-projects',
  components: {
    CytomineTable,
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

    projectCollection() {
      return new ProjectCollection();
    },
    currentPage: sync('listProjects/currentPage'),
    perPage: sync('listProjects/perPage'),
    sortField: sync('listProjects/sortField'),
    sortOrder: sync('listProjects/sortOrder'),
    openedDetails: sync('listProjects/openedDetails')
  },
  methods: {
    formatMomentDate,
    async fetchOntologies() {
      let ontologies = (await OntologyCollection.fetchAll({ light: true })).array;
      ontologies.sort((a, b) => a.name.localeCompare(b.name));
      this.ontologies = ontologies;
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
      await this.fetchOntologies();
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
