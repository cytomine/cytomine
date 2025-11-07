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

        <div class="progress" v-if="status.isUploading || status.isCompleted">
          <b-progress
            :type="status.isCompleted ? 'is-success' : 'is-info'"
            :value="this.uploadFile.progress" format="percent"
            :max="100"
            show-value
          />
        </div>

        <div>
          <b-button v-if="status.isPending" type="is-info" @click="handleTaskUpload">
            {{ $t('upload') }}
          </b-button>
          <b-button v-if="status.isUploading" type="is-primary" @click="handleCancelUpload">
            {{ $t('button-cancel') }}
          </b-button>
          <strong v-if="status.isCancelled" class="has-text-danger">{{ $t('upload-cancelled') }}</strong>
          <strong v-if="status.isCompleted" class="has-text-success">{{ $t('upload-completed') }}</strong>
          <UploadErrorMessage v-if="status.isError" :error="error"/>
        </div>
      </div>

      <div class="column is-narrow has-text-right icon-actions">
        <b-icon v-if="status.isCompleted" icon="check-circle" size="is-medium" />
        <b-icon v-if="status.isCancelled" type="is-danger" icon="exclamation-circle" size="is-medium" />
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
import UploadErrorMessage from '@/components/appengine/UploadErrorMessage.vue';

export default {
  name: 'FileUploadItem',
  components: {
    UploadErrorMessage,
  },
  props: {
    file: {type: File, required: true},
  },
  data() {
    return {
      cancelSource: null,
      error: null,
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
    status() {
      const status = this.uploadFile.status;
      return {
        isCancelled: status === UploadStatus.CANCELLED,
        isCompleted: status === UploadStatus.COMPLETED,
        isError: status === UploadStatus.ERROR,
        isPending: status === UploadStatus.PENDING,
        isUploading: status === UploadStatus.UPLOADING,
      };
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
        const data = (await Cytomine.instance.api.post(
          'app-engine/tasks',
          formData,
          {
            onUploadProgress: (progress) => {
              this.uploadFile.progress = Math.round((progress.loaded / progress.total) * 100);
            },
            cancelToken: this.cancelSource.token,
          }
        )).data;

        if (data.message) {
          this.uploadFile.status = UploadStatus.ERROR;
          this.error = data;
          return;
        }

        this.uploadFile.status = UploadStatus.COMPLETED;
        this.$emit('task-upload:success', data);
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
