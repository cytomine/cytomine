<template>
  <div class="content-wrapper">
    <b-loading :is-full-page="false" :active="loading" />

    <b-message v-if="error" type="is-danger" has-icon icon-size="is-small">
      {{ $t('failed-fetch-tasks') }}
    </b-message>

    <div v-else class="panel">
      <p class="panel-heading">{{ $t('app-engine.task.upload') }}</p>
      <UploadAppButton btnFunc="upload" @taskUploadSuccess="handleTaskUpload" />

      <section class="panel-block">
        <b-field class="file is-centered">
          <b-upload v-model="selectedFiles" multiple drag-drop accept=".zip">
            <section class="section">
              <div class="content has-text-centered">
                <b-icon class="upload-icon" icon="upload" size="is-large" />
                <p class="file-label">{{ $t('upload-placeholder') }}</p>
                <span class="help">{{ $t('upload-support') }}</span>
              </div>
            </section>
          </b-upload>
        </b-field>

        <div v-if="selectedFiles.length > 0">
          <div class="columns">
            <div class="column">
              <strong class="is-size-4">
                {{ $t('files') }} ({{ selectedFiles.length }})
              </strong>
            </div>
            <div class="column has-text-right">
              <b-button type="is-link" size="is-medium">
                {{ $t('upload-all') }}
              </b-button>
            </div>
          </div>

          <div v-for="file in selectedFiles" :key="file.name">
            <FileUploadItem :file="file" @file:remove="handleRemoveFile" @task-upload:success="handleTaskUpload" />
          </div>
        </div>
      </section>

      <p class="panel-heading">{{ $t('app-engine.tasks.installed') }}</p>
      <section class="panel-block lower-section-flex">
        <AppCard v-for="app in applications" :key="app.id" :app="app" />
      </section>
    </div>
  </div>
</template>

<script>
import AppCard from '@/components/appengine/AppCard.vue';
import FileUploadItem from '@/components/appengine/FileUploadItem.vue';
import UploadAppButton from '@/components/appengine/UploadAppButton.vue';
import Task from '@/utils/appengine/task';

export default {
  name: 'AppLocalPage',
  components: {
    AppCard,
    FileUploadItem,
    UploadAppButton,
  },
  data() {
    return {
      applications: [],
      selectedFiles: [],
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
    async handleTaskUpload() {
      try {
        this.applications = await Task.fetchAll();
        this.$notify({type: 'success', text: this.$t('notify-success-task-upload')});
      } catch (error) {
        console.error('Error fetching tasks after upload:', error);
        this.error = error.message;
      }
    },
    handleRemoveFile(file) {
      this.selectedFiles = this.selectedFiles.filter(f => f.name !== file.name);
    }
  },
};
</script>

<style scoped>
.lower-section-flex {
  display: flex;
  flex-direction: row;
  gap: 1%;
  flex-wrap: wrap;
  flex-basis: 30%;
}

.lower-section-flex>* {
  flex-basis: 20%;
  margin: 1em;
}

.panel-block {
  padding-top: 0.8em;
}

.panel-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-icon {
  margin-bottom: 1rem;
}
</style>
