<template>
<span v-if="term" class="ontology-term">
  <div class="color-preview" v-if="term.color" :style="{background: term.color}"></div>
  {{term.name}}
  <span v-if="currentAccount.isDeveloper && term.id > 0"> ({{$t('id')}}: {{term.id}})</span>
</span>
</template>

<script>
import {get} from '@/utils/store-helpers';

export default {
  name: 'cytomine-term',
  props: {
    term: Object
  },
  computed: {
    currentAccount: get('currentUser/account'),
  }
};
</script>

<style scoped>
.color-preview {
  width: 1em;
  height: 1em;
  display: inline-block;
  margin-right: 0.25em;
  border-radius: 0.25em;
  box-shadow: 0 0 1px #777;
  position: relative;
  top: 0.2em;
}
</style>
