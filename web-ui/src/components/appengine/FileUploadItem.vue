<template>
  <div class="box">
    <div class="columns">
      <div class="column is-narrow has-text-centered">
        <b-icon icon="file-archive" size="is-large" />
      </div>

      <div class="column">
        <div class="upload-text">
          <div class="has-text-weight-semibold">{{ file.name }}</div>
          <div class="has-text-grey">{{ formattedFileSize }}</div>
        </div>

        <div class="progress" v-if="isUploading || isCompleted">
          <b-progress :type="isCompleted ? 'is-success' : 'is-info'" :value="this.uploadFile.progress" format="percent"
            :max="100" show-value />
        </div>

        <div class="upload-cta">
          <b-button v-if="isPending" type="is-info" @click="handleTaskUpload">
            {{ $t('upload') }}
          </b-button>
          <b-button v-if="isUploading" type="is-primary" @click="handleCancelUpload">
            {{ $t('button-cancel') }}
          </b-button>
          <strong v-if="isCancelled" class="has-text-danger">{{ $t('upload-cancelled') }}</strong>
          <strong v-if="isCompleted" class="has-text-success">{{ $t('upload-completed') }}</strong>
        </div>
      </div>

      <div class="column is-narrow has-text-right icon-actions">
        <b-icon v-if="isCompleted" icon="check-circle" size="is-medium" />
        <b-icon v-if="isCancelled" icon="exclamation-circle" size="is-medium" />
        <b-button icon-left="times" @click="$emit('file:remove', file)" />
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import filesize from 'filesize';

import {Cytomine} from '@/api';
import {UploadStatus} from '@/utils/app';

export default {
  name: 'FileUploadItem',
  props: {
    file: {type: File, required: true},
  },
  data() {
    return {
      cancelSource: null,
      uploadFile: {
        data: this.file,
        name: this.file.name,
        size: this.file.size,
        progress: 0,
        status: UploadStatus.PENDING,
      },
    };
  },
  computed: {
    isCancelled() {
      return this.uploadFile.status === UploadStatus.CANCELLED;
    },
    isCompleted() {
      return this.uploadFile.status === UploadStatus.COMPLETED;
    },
    isPending() {
      return this.uploadFile.status === UploadStatus.PENDING;
    },
    isUploading() {
      return this.uploadFile.status === UploadStatus.UPLOADING;
    },
    formattedFileSize() {
      return this.file.size ? filesize(this.file.size, {base: 10}) : this.$t('unknown');
    },
  },
  methods: {
    async handleTaskUpload() {
      this.cancelSource = axios.CancelToken.source();

      const formData = new FormData();
      formData.append('task', this.uploadFile.data);

      this.uploadFile.status = UploadStatus.UPLOADING;
      try {
        const response = await Cytomine.instance.api.post(
          'app-engine/tasks',
          formData,
          {
            onUploadProgress: (progress) => {
              this.uploadFile.progress = Math.round((progress.loaded / progress.total) * 100);
            },
            cancelToken: this.cancelSource.token,
          }
        );
        this.uploadFile.status = UploadStatus.COMPLETED;
        this.$emit('task-upload:success', response.data);
      } catch (error) {
        console.error(error);
        this.$emit('task-upload:error');
      }
    },
    handleCancelUpload() {
      this.cancelSource.cancel();
      this.uploadFile.status = UploadStatus.CANCELLED;
    },
  },
};
</script>

<style scoped>
.box {
  padding: 1rem;
}

.box .columns:not(:last-child) {
  margin-bottom: 0 !important;
}

.icon-actions {
  display: inline-flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.progress {
  margin-bottom: 1rem;
}

.upload-text {
  margin-bottom: 1rem;
}
</style>
