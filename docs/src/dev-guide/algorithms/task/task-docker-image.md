---
title: Task Docker image
---

# {{ $frontmatter.title }}

::: warning
App Engine and its Task system are still in BETA. Therefore, all information presented in this documentation is subject to potentially breaking changes.
:::

In order to package the execution environement and implementation of a Task, App Engine relies on Docker images. This section provides guidelines on how to create a Docker image for a Task.

## Task implementation

The implementation of the Task is at the discretion of the developer, who may choose any programming language, technology, or framework, provided it can be encapsulated within a Docker container. The App Engine excutes the Task by launching a container from the specified Docker image and initiating the image's default entrypoint and/or command.

The requirements for implementing the Task are as follows:

- The Docker image's entrypoint or command should initiate the Task's script or program.
- The Task should read input data from the `/inputs` directory and output data to the `/outputs` directory, adhering to the guidelines outlined in the [Task I/O documentation](/dev-guide/algorithms/task/task-io).
- The main process of the Task must exit with a non-zero code to indicate failure and 0 to indicate successful execution.

## How to bundle the Task image

Once the Dockerfile is ready, the image `tar` archive can be created to be included in the Task bundle (which also contains the Task descriptor file).

Creating the `tar` archive for a Task with namespace `com.my.task` and version `1.0.0` involves two steps:

1. build the Docker image and name it using the namespace as a tag:

   ```bash
   docker build -t com/my/task:1.0.0 -f Dockerfile .
   ```

2. save the Docker image as a `tar` archive:

   ```bash
   docker save com/my/task:1.0.0 -o com.my.task-1.0.0.tar
   ```

::: warning
**IMPORTANT**: the image name must be the modified namespace and version, where the modified namespace is simply the Task namespace with `.` replaced by `/`. For example, the namespace `com.my.task` becomes `com/my/task`.
The task version is then appended as a regular Docker image tag: `com/my/task:1.0.0`.
:::

## Testing your Task image

Because the Task implementation is independent from Cytomine and the App Engine, it is possible to test the Task image locally before uploading it to Cytomine:

1. Prepare the input and output directories:

   ```bash
   mkdir -p ./inputs ./outputs
   ```

2. Create the input files required by the Task in the `./inputs` directory following the guidelines in the [Task I/O documentation](/dev-guide/algorithms/task/task-io).

3. Load the Docker image from the `tar` archive:

   ```bash
   docker load -i com.my.task-1.0.0.tar
   ```

4. Execute the Task image as a container, mounting the input and output directories:

   ```bash
   docker run -v $(pwd)/inputs:/inputs -v $(pwd)/outputs:/outputs com/my/task:1.0.0
   ```

At the end of the execution, the outputs should have been generated in the `outputs` directory.
