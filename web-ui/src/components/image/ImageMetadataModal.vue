<template>
<cytomine-modal :active="active" :title="$t('image-metadata')" @close="$emit('update:active', false)">
  <image-metadata :image="image" :error="error" :show-macro="true" />
  <template #footer>
    <button class="button" type="button" @click="$emit('update:active', false)">
      {{$t('button-close')}}
    </button>
  </template>
</cytomine-modal>
</template>

<script>
import CytomineModal from '@/components/utils/CytomineModal.vue';
import ImageMetadata from '@/components/image/ImageMetadata.vue';

export default {
  name: 'image-metadata-modal',
  props: {
    active: Boolean,
    image: Object,
    error: { type: Boolean, default: false }
  },
  components: { ImageMetadata, CytomineModal },
};
</script>

<style lang="scss" scoped>
:deep(.modal-card), :deep(.modal-card-body) {
  width: 800px;
  max-width: 100%;
}
</style>
