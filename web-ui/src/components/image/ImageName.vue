<template>
<span v-if="image">
  <template v-if="image.blindedName">
    <span class="blind-indication">[{{this.$t('blinded-name-indication')}}]</span>
    {{image.blindedName}}
    <template v-if="image.instanceFilename && showBothNames">
      <br>
      <span class="true-name">
        {{image.instanceFilename}} <!-- backend will return this prop iff user is allowed to view it (admin or project manager) -->
      </span>
    </template>
  </template>
  <template v-else>{{image.instanceFilename}}</template>
</span>
</template>

<script>
export default {
  name: 'image-name',
  props: {
    image: Object,
    showBothNames: {type: Boolean, default: false}
  }
};
</script>

<style scoped>
.blind-indication {
  font-size: 0.9em;
  text-transform: uppercase;
}

.true-name {
  margin-top: 0.2em;
  font-size: 0.9em;
  color: #888;
}
</style>
