<template>
  <div class="content-wrapper">
    <div class="box">
      <h1>{{ $t('user-history') }}</h1>

      {{ commands }}
    </div>
  </div>
</template>

<script>
import {Cytomine} from '@/api';

export default {
  name: 'UserHistoryPage',
  data() {
    return {
      commands: [],
    };
  },
  async created() {
    this.commands = await await Cytomine.instance.api.get('/commands');
  },
};
</script>
