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
  methods: {
    onSelect(ids) {
      this.$emit('input', {ids, type: 'annotation'});
    }
  },
};
</script>
