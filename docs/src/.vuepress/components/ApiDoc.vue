<template>
  <div class="api">
    <rapi-doc
      v-if="isLoaded"
      spec-url="https://raw.githubusercontent.com/cytomine/cytomine/main/core/specs/openapi.yml"
      render-style="focused"
      class="rapidoc"
      theme="light"
      show-header="false"
      allow-spec-url-load="false"
      allow-spec-file-load="false"
      text-color="#2c3e50"
      header-color="#2c3e50"
      :primary-color="$cytomine.accentColor"
      :nav-hover-text-color="$cytomine.accentColor"
      nav-bg-color="#ffffff"
      nav-text-color="#2c3e50"
      font-size="large"
      regular-font="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
        Oxygen, Ubuntu, Cantarell, 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif"
      allow-try="false"
      generate-missing-tags="false"
      sort-endpoints-by="summary"
      show-components="false"
      schema-description-expanded="true"
      allow-authentication="false"
      allow-server-selection="false"
      ref="thedoc"
    >
      <div slot="nav-logo" class="vuepress-sidebar">
        <slot name="sidebar"></slot>
      </div>
      <div slot="overview">
        <Content />
      </div>
    </rapi-doc>
  </div>
</template>

<script>
export default {
  name: "ApiDoc",
  data() {
    return {
      isLoaded: false,
    };
  },
  computed: {
    version() {
      return this.$cytomine.editions[0].version;
    }
  },
  mounted() {
    if (!customElements.get("rapi-doc")) {
      const script = document.createElement("script");
      script.type = "module";
      script.src = "https://unpkg.com/rapidoc/dist/rapidoc-min.js";
      script.onload = () => {
        this.isLoaded = true;
        this.$nextTick(() => {
          const el = this.$refs.thedoc;
          if (el) {
            el.addEventListener("before-render", (e) => {
              e.detail.spec.info.version = "Cytomine Community Edition " + this.version;
            });
          }
        });
      };
      document.head.appendChild(script);
    } else {
      this.isLoaded = true;
    }
  }
};
</script>

<style lang="stylus">
.api
  padding $navbarHeight 0 0

  .rapidoc
    height "calc(100vh - %s)" % $navbarHeight
    width 100%

.vuepress-sidebar
  margin: -16px -30px 0 -16px

  .sidebar
    position: unset
    width 100%
    border-right none

    .sidebar-links
      padding-bottom 0

rapi-doc::part(section-navbar)
  border-right 1px solid $borderColor
</style>
