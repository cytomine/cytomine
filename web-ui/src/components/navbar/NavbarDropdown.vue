<template>
<div class="navbar-item has-dropdown is-hoverable">
  <span class="navbar-link" :class="{'is-active': isActive, ...linkClasses}" tabindex="0">
    <i v-if="icon" :class="[iconPack, icon]"></i>
    {{title}}
  </span>
  <div class="navbar-dropdown" :class="classes">
    <slot></slot>
  </div>
</div>
</template>

<script>
export default {
  name: 'navbar-dropdown',
  props: {
    icon: String,
    iconPack: { type: String, default: 'fas' },
    title: String,
    classes: Array,
    linkClasses: Object,
    listPathes: Array
  },
  data() {
    return {
      isActive: false
    };
  },
  watch: {
    '$route.path': {
      handler() {
        if (this.listPathes) {
          this.isActive = !!this.listPathes.find(p => this.$route.path.match(p));
        }
        // required so dropdown doesn't remain open on route change.
        document.activeElement.blur();
      },
      immediate: true
    }
  }
};
</script>

<style>
@media screen and (min-width: 1024px) {
  .navbar-item.is-hoverable:hover .navbar-dropdown {
    display: block;
  }
  .navbar-item.is-hoverable:focus-within:not(:hover) .navbar-dropdown {
    display: none;
  }
}
</style>
