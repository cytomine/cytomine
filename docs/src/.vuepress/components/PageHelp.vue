<template>
  <footer class="page-help">
    <div>
      <h4>Still need help?</h4>
      <NavLink v-for="link in links" :key="link.link" :item="link" class="action-button" />
    </div>
  </footer>
</template>

<script>
import NavLink from "../theme/components/NavLink";

export default {
  name: "PageHelp",
  components: {
    NavLink,
  },
  computed: {
    links() {
      return [
        {
          link: this.$cytomine.urls.githubDiscussions,
          text: "Ask Cytomine community on Github",
        },
        {
          link: this.$cytomine.socials.imagesc,
          text: "Ask Cytomine community on image.sc",
        },
        {
          link: `mailto:${this.$cytomine.email}`,
          text: "Contact us",
        },
      ];
    },
  },
};
</script>

<style lang="stylus">
@require '../theme/styles/wrapper.styl'

.page-help
  @extend $wrapper
  padding-top 1rem
  padding-bottom 1rem
  overflow auto
  div
    background #f4f4f4
    border-radius 4px
    padding 1rem

    h4
      margin 0

    .action-button
      display inline-block
      font-size 0.9rem
      color #fff
      background-color $accentColor
      padding 0.4rem 0.8rem
      margin-top 1rem
      margin-right 1rem
      border-radius 4px
      transition background-color .1s ease
      box-sizing border-box
      border-bottom 1px solid darken($accentColor, 10%)
      &:hover
        background-color lighten($accentColor, 10%)
      .icon.outbound
          color #fff


  .edit-link
    display inline-block
    a
      color lighten($textColor, 25%)
      margin-right 0.25rem
  .last-updated
    float right
    font-size 0.9em
    .prefix
      font-weight 500
      color lighten($textColor, 25%)
    .time
      font-weight 400
      color #767676
</style>
