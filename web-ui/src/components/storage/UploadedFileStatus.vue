<template>
<div v-if="!iconOnly">
  <span v-if="!isSuccessful || file.nbChildren === undefined" class="tag" :class="tagClass" :data-status="result">
    {{ $t(labels[file.status]) }}
  </span>
  <div v-else class="tags has-addons">
    <span class="tag" :class="tagClass" :data-status="result">{{ $t(labels[file.status]) }}</span>
    <span class="tag is-light">{{ $tc("count-files", file.nbChildren + 1, { count: file.nbChildren + 1 }) }}</span>
  </div>
</div>
<span v-else :class="['icon', textClass]">
  <i :class="['fas', iconClass]" :title="$t(labels[file.status])"></i>
</span>
</template>

<script>
import {UploadedFileStatus} from '@/constants/UploadedFileStatus';

export default {
  name: 'uploaded-file-status',
  props: {
    file: Object,
    iconOnly: {type: Boolean, default: false}
  },
  computed: {
    isSuccessful() {
      return this.file.status === UploadedFileStatus.CONVERTED || this.file.status === UploadedFileStatus.DEPLOYED;
    },
    labels() {
      return {
        [UploadedFileStatus.UPLOADED]: 'uploaded',
        [UploadedFileStatus.CONVERTED]: 'converted',
        [UploadedFileStatus.DEPLOYED]: 'deployed',
        [UploadedFileStatus.ERROR_FORMAT]: 'error-format',
        [UploadedFileStatus.ERROR_CONVERSION]: 'error-convert',
        [UploadedFileStatus.EXTRACTED]: 'uncompressed',
        [UploadedFileStatus.ERROR_DEPLOYMENT]: 'error-deployment',
        [UploadedFileStatus.DETECTING_FORMAT]: 'detecting-format',
        [UploadedFileStatus.EXTRACTING_DATA]: 'extracting-data',
        [UploadedFileStatus.ERROR_EXTRACTION]: 'error-extraction',
        [UploadedFileStatus.CONVERTING]: 'converting',
        [UploadedFileStatus.DEPLOYING]: 'deploying',
        [UploadedFileStatus.UNPACKING]: 'unpacking',
        [UploadedFileStatus.ERROR_UNPACKING]: 'error-unpacking',
        [UploadedFileStatus.CHECKING_INTEGRITY]: 'checking-integrity',
        [UploadedFileStatus.ERROR_INTEGRITY]: 'error-integrity',
        [UploadedFileStatus.UNPACKED]: 'unpacked',
      };
    },
    result() {
      switch (this.file.status) {
        case UploadedFileStatus.UPLOADED:
        case UploadedFileStatus.DETECTING_FORMAT:
        case UploadedFileStatus.EXTRACTING_DATA:
        case UploadedFileStatus.CONVERTING:
        case UploadedFileStatus.DEPLOYING:
        case UploadedFileStatus.EXTRACTED:
        case UploadedFileStatus.UNPACKING:
        case UploadedFileStatus.CHECKING_INTEGRITY:
          return 'info';
        case UploadedFileStatus.CONVERTED:
        case UploadedFileStatus.DEPLOYED:
        case UploadedFileStatus.UNPACKED:
          return 'success';
        case UploadedFileStatus.ERROR_FORMAT:
        case UploadedFileStatus.ERROR_CONVERSION:
        case UploadedFileStatus.ERROR_DEPLOYMENT:
        case UploadedFileStatus.ERROR_EXTRACTION:
        case UploadedFileStatus.ERROR_UNPACKING:
        case UploadedFileStatus.ERROR_INTEGRITY:
          return 'danger';
        default:
          return null;
      }
    },
    tagClass() {
      return 'is-' + this.result;
    },
    textClass() {
      return 'has-text-' + this.result;
    },
    // eslint-disable-next-line vue/return-in-computed-property
    iconClass() {
      switch (this.result) {
        case 'info':
          return 'fa-spinner';
        case 'success':
          return 'fa-check-square';
        case 'danger':
          return 'fa-times-circle';
      }
    }
  }
};
</script>

<style scoped>
.fas {
  font-size: 1.3em;
}
</style>
