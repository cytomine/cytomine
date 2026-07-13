<template>
  <b-tabs v-model="activeTab" expanded>
    <b-tab-item :label="$t('image')" icon="image">
      <ImageMultiSelect :key="'image-' + activeTab" @update:model-value="onMultiSelect($event, 'image')" />
    </b-tab-item>

    <b-tab-item :label="$t('geometry')" icon="draw-polygon">
      <AnnotationMultiSelect :key="'annotation-' + activeTab" @update:model-value="onMultiSelect($event, 'annotation')"/>
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
  methods: {
    onMultiSelect(ids, type) {
      this.$emit('update:modelValue', {ids, type});
    }
  },
};
</script>
