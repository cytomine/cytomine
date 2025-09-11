<template>
  <div class="footer">
    {{ footer }}
  </div>
</template>

<script>
export default {
  name: "Footer",
  computed: {
    footer() {
      return this.$cytomine.footer;
    },
  },
};
</script>

<style scoped lang="stylus">
.footer {
  padding 2rem
  text-align center
  color lighten($textColor, 25%)
}
</style>
