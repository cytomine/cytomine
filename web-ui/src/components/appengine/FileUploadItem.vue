<template>
  <div class="box">
    <div class="columns is-vcentered is-gapless">
      <div class="column is-narrow has-text-centered">
        <b-icon icon="file-archive" size="is-large" />
      </div>

      <div class="column">
        <div class="columns is-vcentered is-gapless">
          <div class="column">
            <div class="has-text-weight-semibold">{{ file.name }}</div>
            <div class="has-text-grey">{{ formattedFileSize }}</div>
          </div>

          <div class="column is-narrow has-text-right">
            <b-button icon-left="times" @click="$emit('file:remove', file)" />
          </div>
        </div>
      </div>
    </div>

    Progress: {{ this.uploadFile.progress }}%

    <div class="column">
      <b-button type="is-primary" size="is-medium" @click="handleTaskUpload">
        {{ $t('upload') }}
      </b-button>
      <b-button type="is-primary" size="is-medium" @click="handleCancelUpload">
        {{ $t('button-cancel') }}
      </b-button>
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
      uploadFile: null,
    };
  },
  computed: {
    formattedFileSize() {
      return this.file.size ? filesize(this.file.size, {base: 10}) : this.$t('unknown');
    },
  },
  created() {
    this.uploadFile = {
      data: this.file,
      name: this.file.name,
      size: this.file.size,
      progress: 0,
      status: UploadStatus.PENDING,
    };
  },
  methods: {
    async handleTaskUpload() {
      this.cancelSource = axios.CancelToken.source();

      const formData = new FormData();
      formData.append('task', this.uploadFile.data);

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
        this.$emit('task-upload:success', response.data);
      } catch (error) {
        console.error(error);
        this.$emit('task-upload:error');
      }
    },
    handleCancelUpload() {
      this.cancelSource.cancel();
    },
  },
};
</script>
