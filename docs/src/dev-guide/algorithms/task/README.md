---
title: Cytomine Task
---

# {{ $frontmatter.title }}

::: warning
App Engine and its Task system are still in BETA. Therefore, all information presented in this documentation is subject to potentially breaking changes.
:::

A Task is a time-limited process that takes static inputs and produces static outputs represented by files and directories. A Task is defined by:

1. a _docker image_ encapsulating the environment and procudure of the Task
2. a _descriptor file_ providing both general and technical information about the Task itself

In order to upload a Task on Cytomine, it must be bundled as a `zip` archive containing the docker image and the descriptor file. The Docker image must be [saved into a `tar` archive](/dev-guide/algorithms/task/task-docker-image) before being included in the bundle.

## How to create a Task

1. Write your Task descriptor based on the [Task descriptor reference](/dev-guide/algorithms/task/descriptor-reference)
1. Implement your Task in such a way that the script or program running the Task can be containerised
1. Create a Dockerfile to build the [Task container image](/dev-guide/algorithms/task/task-docker-image)
1. [Build and save the Docker image](/dev-guide/algorithms/task/task-docker-image#how-to-bundle-the-task-image) as a `tar` archive
1. Bundle the saved Docker image and the Task descriptor into a `zip` archive
