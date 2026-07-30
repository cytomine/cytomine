<template>
<span class="tag" :class="tagClass">
  {{$t(label)}}
</span>
</template>

<script>
export default {
  props: {
    image: Object
  },
  computed: {
    tagClass() {
      if (this.image.reviewed) {
        return 'is-success';
      }
      if (this.image.inReview) {
        return 'is-info';
      }
      return 'is-light';
    },
    label() {
      if (this.image.reviewed) {
        return 'reviewed';
      }
      if (this.image.inReview) {
        return 'in-review';
      }
      return 'none';
    }
  }
};
</script>
