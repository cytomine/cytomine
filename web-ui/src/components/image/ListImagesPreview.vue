<template>
<div class="columns" v-if="images && images.length">
  <div class="column" v-for="image in images" :key="image.image">
    <image-preview :image="image" :blindMode="project.blindMode" />
  </div>
  <div class="column is-narrow vertical-center">
    <router-link class="button" :to="`/project/${this.project.id}/images`">{{$t('button-view-all')}}</router-link>
  </div>
</div>
<div v-else>
  {{$t('no-image-recently-opened')}}
</div>
</template>

<script>
import {get} from '@/utils/store-helpers';

import {ImageInstanceCollection} from '@/api';

import ImagePreview from './ImagePreview.vue';

export default {
  name: 'list-images-preview',
  components: {ImagePreview},
  props: {
    project: Object,
    nbRecent: {
      type: Number,
      default: 3
    }
  },
  data() {
    return {
      images: []
    };
  },
  computed: {
    currentUser: get('currentUser/user')
  },
  async created() {
    this.images = await ImageInstanceCollection.fetchLastOpened({
      max: this.nbRecent,
      project: this.project.id,
      user: this.currentUser.id
    });
  }
};
</script>

<style scoped>
.column:not(.is-narrow) {
  max-width: 12.5rem;
  min-width: 12.5rem;
}

.vertical-center {
  display: flex;
  flex-direction: row;
  align-items: center;
}
</style>
