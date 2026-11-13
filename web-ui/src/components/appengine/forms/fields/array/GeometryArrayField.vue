<template>
  <AnnotationMultiSelect @input="onSelect" />
</template>

<script>
import AnnotationMultiSelect from '@/components/appengine/forms/fields/array/AnnotationMultiSelect.vue';

export default {
  name: 'GeometryArrayField',
  components: {
    AnnotationMultiSelect,
  },
  props: {
    modelValue: {},
  },
  emits: ['update:modelValue'],
  methods: {
    onSelect(ids) {
      this.$emit('update:modelValue', { ids, type: 'annotation' });
    }
  },
};
</script>
