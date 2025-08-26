const content = require("./site-config/content.json");
const navbar = require("./site-config/navbar.js");
const sidebar = require("./site-config/sidebar.js");

module.exports = {
  /**
   * Ref：https://v1.vuepress.vuejs.org/config/#title
   */
  title: content.title,
  /**
   * Ref：https://v1.vuepress.vuejs.org/config/#description
   */
  description: content.description,

  /**
   * Extra tags to be injected to the page HTML `<head>`
   *
   * ref：https://v1.vuepress.vuejs.org/config/#head
   */
  head: [
    ["meta", { name: "theme-color", content: content.style.accentColor }],
    ["meta", { name: "apple-mobile-web-app-capable", content: "yes" }],
    ["meta", { name: "apple-mobile-web-app-status-bar-style", content: "black" }],
    ["link", { rel: "icon", href: content.icon }],
    [
      "link",
      {
        rel: "stylesheet",
        type: "text/css",
        href: "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css",
      },
    ],
  ],

  /**
   * Theme configuration, here is the default theme configuration for VuePress.
   *
   * ref：https://v1.vuepress.vuejs.org/theme/default-theme-config.html
   */
  themeConfig: {
    logo: content.logo,
    nextLinks: true,
    prevLinks: true,

    repo: content.socials.github,
    editLinks: false,
    docsRepo: content.urls.docsRepo,
    docsDir: "docs",
    lastUpdated: true,

    smoothScroll: true,
    nav: navbar(content),
    sidebarDepth: 2,
    sidebar: {
      "/user-guide/": sidebar.userGuide,
      "/admin-guide/": sidebar.adminGuide,
      "/dev-guide/": sidebar.devGuide,
    },
    algolia: {
      apiKey: "",
      indexName: "",
    },
  },
  markdown: {
    pageSuffix: "",
    lineNumbers: true,
  },

  stylus: {
    preferPathResolver: "webpack",
    // VuePress uses stylus-loader v3.0.2, so we need to use a deprecated syntax
    // https://github.com/webpack-contrib/stylus-loader/blob/v3.0.2/index.js#L77
  },

  /**
   * Apply plugins，ref：https://v1.vuepress.vuejs.org/zh/plugin/
   */
  plugins: [
    ["@vuepress/plugin-back-to-top", true],
    [
      "@vuepress/plugin-medium-zoom",
      {
        options: {
          background: "rgba(255,255,255,0.95)",
        },
      },
    ],
    ["@vuepress/plugin-nprogress", true],
    ["vuepress-plugin-clean-urls", { normalSuffix: "" }],
    ["vuepress-plugin-redirect-frontmatter"],
    ["vuepress-plugin-serve"],
    [
      "vuepress-plugin-dehydrate",
      {
        // disable SSR
        noSSR: "404.html",
        // remove scripts
        noScript: [
          // support glob patterns
          "foo/*.html",
          "**/static.html",
        ],
      },
    ],
  ],

  extraWatchFiles: [
    ".vuepress/site-config/content.json",
    ".vuepress/site-config/navbar.js",
    ".vuepress/site-config/sidebar.js",
  ],
};
