<template>
  <div class="content-wrapper">
    <b-loading :is-full-page="false" :active="loading" />

    <b-message v-if="error" type="is-danger" has-icon icon-size="is-small">
      {{ $t('failed-fetch-tasks') }}
    </b-message>

    <div v-else>
      <AppUploadPanel @task-upload:success="handleTaskUploaded" />

      <AppListPanel :applications="applications" />
    </div>
  </div>
</template>

<script>
import AppListPanel from '@/components/appengine/panels/AppListPanel.vue';
import AppUploadPanel from '@/components/appengine/panels/AppUploadPanel.vue';
import Task from '@/utils/appengine/task';

export default {
  name: 'AppLocalPage',
  components: {
    AppListPanel,
    AppUploadPanel,
  },
  data() {
    return {
      applications: [],
      error: '',
      loading: true,
    };
  },
  async created() {
    try {
      this.applications = await Task.fetchAll();
    } catch (error) {
      this.error = error.message;
    } finally {
      this.loading = false;
    }
  },
  methods: {
    handleTaskUploaded(task) {
      this.applications.push(task);
    },
  }
};
</script>
