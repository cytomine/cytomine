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

    <div class="column">
      <b-button type="is-primary" size="is-medium">{{ $t('upload') }}</b-button>
    </div>
  </div>
</template>

<script>
import filesize from 'filesize';

export default {
  name: 'FileUploadItem',
  props: {
    file: {type: File, required: true},
  },
  computed: {
    formattedFileSize() {
      return this.file.size ? filesize(this.file.size, {base: 10}) : this.$t('unknown');
    },
  },
};
</script>
