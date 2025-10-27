<template>
  <div class="panel">
    <p class="panel-heading">{{ $t('app-engine.task.upload') }}</p>

    <section class="panel-block">
      <b-field class="file is-centered container">
        <b-upload v-model="selectedFiles" multiple drag-drop accept=".zip" expanded>
          <section class="section">
            <div class="content has-text-centered">
              <b-icon class="upload-icon" icon="upload" size="is-large" />
              <p class="file-label">{{ $t('upload-placeholder') }}</p>
              <span class="help">{{ $t('upload-support') }}</span>
            </div>
          </section>
        </b-upload>
      </b-field>

      <div class="container" v-if="selectedFiles.length > 0">
        <div class="columns is-vcentered">
          <div class="column">
            <strong class="is-size-4">
              {{ $t('files') }} ({{ selectedFiles.length }})
            </strong>
          </div>
          <div class="column has-text-right">
            <b-button type="is-link" size="is-medium" @click="handleUploadAll">
              {{ $t('upload-all') }}
            </b-button>
          </div>
        </div>

        <div class="file-list">
          <div v-for="file in selectedFiles" :key="file.name">
            <FileUploadItem
              ref="fileUploadChildren"
              :file="file"
              @file:remove="handleRemoveFile"
              @task-upload:success="handleTaskUploaded"
            />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import FileUploadItem from '@/components/appengine/FileUploadItem.vue';

export default {
  name: 'AppUploadPanel',
  components: {
    FileUploadItem,
  },
  data() {
    return {
      selectedFiles: [],
    };
  },
  methods: {
    handleTaskUploaded(task) {
      this.$emit('task-upload:success', task);
      this.$notify({type: 'success', text: this.$t('notify-success-task-upload')});
    },
    handleRemoveFile(file) {
      this.selectedFiles = this.selectedFiles.filter(f => f.name !== file.name);
    },
    async handleUploadAll() {
      if (!this.$refs.fileUploadChildren) {
        return;
      }

      try {
        await Promise.all(
          this.$refs.fileUploadChildren.map(child => child.handleTaskUpload())
        );
      } catch (error) {
        console.error(error);
        this.$notify({
          type: 'error',
          text: error.message,
        });
      }
    },
  },
};
</script>

<style scoped>
.file {
  margin-top: 1rem;
  margin-bottom: 1rem;
}

.file-label {
  justify-content: center;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.upload-icon {
  margin-bottom: 1rem;
}
</style>
