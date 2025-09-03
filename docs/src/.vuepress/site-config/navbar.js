module.exports = (content) => [
  {
    text: "User Guide",
    items: [
      {
        text: "Overview",
        items: [
          {text: "Introduction", link: "/user-guide/"},
          {text: "Getting started", link: "/user-guide/getting-started/"},
        ],
      },
      {
        text: "Concepts",
        items: [
          {text: "Role", link: "/user-guide/role/"},
          {text: "Administration", link: "/user-guide/administration/"},
          {text: "Project", link: "/user-guide/project/"},
          {text: "Ontology", link: "/user-guide/ontology/"},
          {text: "Image Viewer", link: "/user-guide/image-viewer/"},
          {text: "Image Group", link: "/user-guide/image-group/"},
          {text: "Annotations", link: "/user-guide/annotations/"},
          {text: "Annotation Link", link: "/user-guide/annotation-link/"},
          {text: "Storage", link: "/user-guide/storage/"},
        ],
      },
    ],
  },
  {
    text: "Administrator Guide",
    items: [
      {
        text: "Cytomine",
        items: [
          {text: "Installation", link: "/admin-guide/ce/installation"},
          {text: "Configuration", link: "/admin-guide/ce/configuration"},
          {text: "Backup", link: "/admin-guide/ce/backup"},
          {text: "Troubleshooting", link: "/admin-guide/ce/troubleshooting"},
          {text: "Uninstallation", link: "/admin-guide/ce/uninstallation"},
          {text: "Architecture", link: "/admin-guide/ce/architecture"},
        ],
      },
      {
        text: "Compute Clusters",
        items: [
          {text: "MicroK8s", link: "/admin-guide/clusters/microk8s/"},
        ],
      },
    ],
  },
  {
    text: "Developer Guide",
    items: [
      {
        text: "Play with Cytomine data",
        items: [
          {text: "Cytomine is RESTful", link: "/dev-guide/"},
          {
            text: "API client library for Python",
            link: "/dev-guide/clients/python/installation",
          },
          {
            text: "API Reference",
            link: "/dev-guide/api/reference",
          }
        ],
      },
      {
        text: "Integrate your algorithms",
        items: [
          {text: "Cytomine Task", link: "/dev-guide/algorithms/task/"},
        ],
      },
      {
        text: "Cytomine Architecture",
        items: [{text: "Architecture", link: "/dev-guide/architecture/"}],
      },
    ],
  },
  {
    text: "Community",
    items: [
      {
        text: "Cytomine project",
        items: [
          {text: "Code of Conduct", link: "/community/code-of-conduct"},
        ],
      },
      {
        text: "Repository",
        items: [
          {text: "GitHub", link: content.repositories.cytomine},
        ],
      },
      {
        text: "Discussions",
        items: [
          {text: "Image.sc", link: content.socials.imagesc},
          {text: "GitHub", link: content.urls.githubDiscussions},
        ],
      },
    ],
  },
  {
    text: "About",
    link: content.urls.siteUrl,
  },
];
