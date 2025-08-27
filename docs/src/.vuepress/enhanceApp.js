/**
 * Client app enhancement file.
 *
 * https://v1.vuepress.vuejs.org/guide/basic-config.html#app-level-enhancements
 */

const content = require("./site-config/content.json");

export default ({ Vue }) => {
  Vue.mixin({
    computed: {
      $cytomine() {
        return content;
      },
    },
  });
};
