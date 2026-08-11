<template>
  <b-carousel
      autoplay repeat
      :arrow-hover="false" :pause-hover="false" :pause-info="false" :indicator="false"
      :interval="7500"
      animated="fade"
      icon-size="small"
      icon-next="angle-right"
      icon-prev="angle-left"
      class="has-background-light">
    <b-carousel-item v-for="image in imageGroup.imageInstances" :key="`${imageGroup.id}-${image.id}`">
      <div class="has-text-centered">
        <image-thumbnail
            :extra-parameters="{authorization: 'Bearer ' + shortTermToken}"
            :key="`${imageGroup.id}-${image.thumb}`"
            :size="128"
            :url="image.thumb"
        />
      </div>
    </b-carousel-item>
  </b-carousel>
</template>

<script>
import { get } from '@/utils/store-helpers';

import ImageThumbnail from '@/components/image/ImageThumbnail.vue';

export default {
  name: 'image-group-preview',
  components: { ImageThumbnail },
  props: {
    imageGroup: { type: Object },
  },
  computed: {
    shortTermToken: get('currentUser/shortTermToken'),
  }
};
</script>

<style scoped>
:deep(.image-thumbnail) {
  max-height: 4rem;
  max-width: 10rem;
}

.carousel {
  min-height: auto;
  height: 4rem;
  width: 10rem;
}

:deep(.carousel-arrow .icon.has-icons-right) {
  color: #3273dc;
  right: 0.5rem;
}

:deep(.carousel-arrow .icon.has-icons-left) {
  color: #3273dc;
  left: 0.5rem;
}
</style>
