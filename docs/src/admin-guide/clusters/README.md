---
title: Clusters
---

# {{ $frontmatter.title }}

::: warning
The compute clusters are only for the Community Edition!
:::

A compute cluster is a system that enables the distribution and execution of computational tasks across multiple machines or nodes.
It allows the efficient execution of Cytomine Tasks and Apps.

By integrating compute clusters with Cytomine, you can ensure that resource-intensive tasks are executed in a timely manner. This setup allows Cytomine to handle large execution of complex algorithms, and computational workflows, while offering the flexibility to scale resources as needed based on the demands of each task.

At this time, Cytomine only supports MicroK8s as the compute cluster solution. While Slurm and other cluster management tools are widely used for distributed computing, we have chosen MicroK8s for its lightweight, and easy-to-deploy nature. This ensures a streamlined setup and efficient task execution for Cytomine Apps and Tasks. Future support for other cluster management systems may be considered, but for now, [MicroK8s](/admin-guide/clusters/microk8s/) is the recommended and fully supported solution.
