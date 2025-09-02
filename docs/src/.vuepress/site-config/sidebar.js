module.exports = {
  userGuide: [
    {
      title: "Overview",
      collapsable: false,
      children: [
        ["/user-guide/", "Introduction"],
        ["/user-guide/getting-started", "Getting started"],
      ],
    },
    {
      title: "Concepts",
      collapsable: false,
      children: [
        ["/user-guide/role", "Role"],
        ["/user-guide/administration", "Administration"],
        ["/user-guide/project", "Project"],
        ["/user-guide/ontology", "Ontology"],
        ["/user-guide/image-viewer", "Image viewer"],
        ["/user-guide/image-group", "Image Group"],
        ["/user-guide/annotations", "Annotations"],
        ["/user-guide/annotation-link", "Annotation Link"],
        ["/user-guide/storage", "Storage"],
      ],
    },
  ],

  adminGuide: [
    {
      title: "Community Edition (CE)",
      collapsable: true,
      children: [
        ["/admin-guide/ce/requirements", "Requirements"],
        ["/admin-guide/ce/installation", "Installation"],
        ["/admin-guide/ce/upgrade", "Upgrade"],
        ["/admin-guide/ce/backup", "Backup and restore"],
        ["/admin-guide/ce/troubleshooting", "Troubleshooting"],
        ["/admin-guide/ce/uninstallation", "Uninstallation"],
        ["/admin-guide/ce/architecture", "Architecture"],
      ],
    },
    {
      title: "Compute Clusters",
      collapsable: true,
      children: [
        ["/admin-guide/clusters/", "Introduction"],
        {
          title: "MicroK8s",
          collapsable: true,
          children: [
            ["/admin-guide/clusters/microk8s/", "Introduction"],
            ["/admin-guide/clusters/microk8s/installation", "Installation"],
            ["/admin-guide/clusters/microk8s/troubleshooting", "Troubleshooting"],
            ["/admin-guide/clusters/microk8s/uninstallation", "Uninstallation"],
          ],
        },
      ],
    },
  ],

  devGuide: [
    {
      title: "Play with Cytomine data",
      collapsable: true,
      children: [
        ["/dev-guide/", "Cytomine is RESTful"],
        {
          title: "API client for Python",
          collapsable: true,
          children: [
            ["/dev-guide/clients/python/installation", "Installation"],
            ["/dev-guide/clients/python/usage", "Usage"],
            ["/dev-guide/clients/python/examples", "Examples"],
          ],
        },
        ["/dev-guide/api/reference", "API Reference"],
      ],
    },
    {
      title: "Integrate your algorithms",
      collapsable: true,
      children: [
        {
          title: "Cytomine Task",
          collapsable: true,
          children: [
            ["/dev-guide/algorithms/task/", "Concepts"],
            ["/dev-guide/algorithms/task/task-docker-image", "Task Docker image"],
            ["/dev-guide/algorithms/task/task-io", "Task I/O"],
            ["/dev-guide/algorithms/task/descriptor-reference", "Task descriptor reference"],
            ["/dev-guide/algorithms/task/example", "Example"],
          ],
        },
      ],
    },
    {
      title: "Cytomine Architecture",
      collapsable: true,
      children: [
        ["/dev-guide/architecture/", "Architecture"],
        {
          title: "Components",
          collapsable: false,
          children: [
            ["/dev-guide/architecture/pims", "PIMS"],
          ],
        },
      ],
    },
  ],
};
