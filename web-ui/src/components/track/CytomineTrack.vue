<template>
<span v-if="track">
  <i class="far fa-square color-preview" v-if="track.color" :style="{color: track.color}"></i>
  {{track.name}}
  <span v-if="currentAccount.isDeveloper && track.id > 0"> ({{$t('id')}}: {{track.id}})</span>
</span>
</template>

<script>
import {get} from '@/utils/store-helpers';

export default {
  name: 'cytomine-track',
  props: {
    track: Object
  },
  computed: {
    currentAccount: get('currentUser/account'),
  }
};
</script>

<style scoped>
  .color-preview {
    margin-right: 0.25em;
  }
</style>
