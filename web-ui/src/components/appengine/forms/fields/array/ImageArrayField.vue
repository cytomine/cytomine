<template>
  <b-tabs v-model="activeTab" expanded>
    <b-tab-item :label="$t('image')" icon="image">
      <ImageMultiSelect :key="'image-' + activeTab" @input="$emit('input', $event)" />
    </b-tab-item>

    <b-tab-item :label="$t('geometry')" icon="draw-polygon">
      <AnnotationMultiSelect :key="'annotation-' + activeTab" @input="$emit('input', $event)" />
    </b-tab-item>
  </b-tabs>
</template>

<script>
import AnnotationMultiSelect from '@/components/appengine/forms/fields/array/AnnotationMultiSelect.vue';
import ImageMultiSelect from '@/components/appengine/forms/fields/array/ImageMultiSelect';

export default {
  name: 'ImageArrayField',
  components: {
    AnnotationMultiSelect,
    ImageMultiSelect,
  },
  data() {
    return {
      activeTab: 0,
    };
  },
};
</script>
