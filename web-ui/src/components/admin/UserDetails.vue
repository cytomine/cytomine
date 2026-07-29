<template>
<div class="details-wrapper">
  <b-loading :active="loading" :is-full-page="false" class="small" />
  <table v-if="!loading" class="table">
    <tbody>
      <tr v-if="currentAccount.isDeveloper">
        <td>{{$t('id')}}</td>
        <td colspan="2">{{user.id}}</td>
      </tr>
      <tr>
        <td>{{$t('projects')}}</td>
        <td>
          {{projects ? projects.length : '?'}}
        </td>
        <td>
          <b-collapse v-if="projects && projects.length" :open="false">
            <template #trigger="{open}">
              <button class="button is-small">
                {{$t(open ? 'button-hide' : 'button-show')}}
              </button>
            </template>
            <ul>
              <li v-for="project in projects" :key="project.id">
                <router-link :to="`/project/${project.id}`">
                  {{ project.name }}
                </router-link>
              </li>
            </ul>
          </b-collapse>
        </td>
      </tr>
      <tr>
        <td>{{$t('managed-projects')}}</td>
        <td>
          {{managedProjects ? managedProjects.length : '?'}}
        </td>
        <td>
          <b-collapse v-if="managedProjects && managedProjects.length" :open="false">
            <template #trigger="{open}">
              <button class="button is-small">
                {{$t(open ? 'button-hide' : 'button-show')}}
              </button>
            </template>
            <ul>
              <li v-for="project in managedProjects" :key="project.id">
                <router-link :to="`/project/${project.id}`">
                  {{ project.name }}
                </router-link>
              </li>
            </ul>
          </b-collapse>
        </td>
      </tr>
      <tr>
        <td>{{$t('user-annotations')}}</td>
        <td>
          {{nbAnnotations == null ? '?' : nbAnnotations}}
        </td>
        <td></td>
      </tr>
    </tbody>
  </table>
</div>
</template>

<script>
import {get} from '@/utils/store-helpers';
import {ProjectCollection} from '@/api';

export default {
  props: {
    user: Object
  },
  data() {
    return {
      loading: true,
      projects: null,
      managedProjects: null,
      nbAnnotations: null,
      showProjects: false,
      showManagedProjects: false
    };
  },
  computed: {
    currentAccount: get('currentUser/account')
  },
  methods: {
    async fetchProjects() {
      this.projects = (await ProjectCollection.fetchAll({
        filterKey: 'user',
        filterValue: this.user.id,
        light: true
      })).array;
    },
    async fetchManagedProjects() {
      this.managedProjects = (await ProjectCollection.fetchAll({
        filterKey: 'user',
        filterValue: this.user.id,
        light: true,
        admin: true
      })).array;
    },
    async fetchNbAnnotations() {
      this.nbAnnotations = await this.user.fetchNbAnnotations();
    },
  },
  async created() {
    await Promise.all([
      this.fetchProjects(),
      this.fetchManagedProjects(),
      this.fetchNbAnnotations()
    ].map(p => p.catch(e => console.log(e)))); // ignore errors (handled in template) and ensure all promises finish, even if some errors occur in the process
    this.loading = false;
  }
};
</script>

<style scoped>
.table {
  background: none;
}

.details-wrapper {
  min-height: 3em;
  position: relative;
}

td:first-child {
  font-weight: 600;
}

td:not(:last-child) {
  white-space: nowrap;
}

td:last-child {
  width: 100%;
}
</style>
