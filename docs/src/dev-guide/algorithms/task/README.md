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
1. [Build and save the Docker image](/dev-guide/algorithms/task/task-docker-image#how-to-bundle-the-task-image) as a `tar` archive, the name of the archive should be formatted following `{namespace}-{version}.tar` and matching `configuration.image.file` in descriptor 
1. Bundle the saved Docker image and the Task descriptor into a `zip` archive

## Run

To run a task in the app-engine it creates a context for the execution to manage inputs provisioning, outputs generation, the `Run` has different states tracking the flow of execution.

| STATE        |       DESCRIPTION                                                                    |
| -------------| ------------------------------------------------------------------------------------ |
| CREATED      |      Created but not all inputs provisioned                                          |
| PROVISIONED  |      Ready to be executed, all inputs have been provisioned                          |
| QUEUING      |      Submitting the task to the execution environment                                |
| QUEUED       |      Evaluated successfully by execution environment                                 |
| RUNNING      |      Task running in the execution environment                                       |
| FAILED       |      An error occurred and stopped the process of executing the task (terminal state)|
| PENDING      |      Pending execution on the execution environment                                  |
| FINISHED     |      Task successfully executed and outputs available (terminal state)               |

## Storage

The app-engine controls provisioning of inputs to tasks and outputs back to consumers like `core` in Cytomine, every `Run` has its separate storage 
that containts the provisioned inputs and the generated outputs, and tasks metadata files like descriptors or logos.
