---
title: Complete walkthrough example
---

# {{ $frontmatter.title }}

This section provides a step-by-step guide to integrate your algorithm as a Cytomine task. It covers:

1. Creating a simple algorithm
2. Writing the Dockerfile
3. Building the Docker image
4. Testing the Docker image
5. Writing the descriptor
6. Packagig the task bundle
7. Uploading the task to Cytomine

## Create a simple algorithm

The first step is to create your algorithm. In this example, the file is expected to be located at `app/app.py`.
In this example, the algorithm will generate a sequence of numbers given a specified size and compute their sum:

```python
import numpy as np


if __name__ == "__main__":
    with open("/inputs/size", "r") as f:
        size = int(f.read())

    array = np.random.random(size=size)
    total = np.sum(array)

    with open("/outputs/sum", "w") as f:
        f.write(str(total))
```

Notice that the algorithm reads inputs from an `inputs` directory and outputs to an `outputs` directory as explained in [Task implementation](/dev-guide/algorithms/task/task-docker-image#task-implementation).

## Write a Dockerfile

After creating your algorithm, you will need to package it inside a Docker image. For this example, the Dockerfile is as follows:

```Dockerfile
FROM python:3.12.3-alpine

WORKDIR /app

COPY . /app

RUN pip install --no-cache-dir -r requirements.txt

CMD ["python", "app/app.py"]
```

## Build the Docker image

The next step is to build the Docker image as explained in [How to bundle the task image](/dev-guide/algorithms/task/task-docker-image#how-to-bundle-the-task-image).

```bash
sudo docker build -t cytomineuliege/example:0.1.0 -f Dockerfile .
```

## Test the Docker image

Now you can test your task to ensure everything runs smoothly locally:

Create the `inputs` and `outputs` directories:

```bash
mkdir -p ./inputs ./outputs
```

Create the expected input inside the `inputs` directory.
In this example, the file is expected to be named `size` and contain the following:

```text
10
```

Execute the Docker image as a container, mounting the `inputs` and `outputs` directories:

```bash
sudo docker run --rm -v $(pwd)/inputs:/inputs -v $(pwd)/outputs:/outputs cytomineuliege/example:0.1.0
```

At the end of the execution, a file named `sum` should be generated in the `outputs` directory.

## Write the descriptor

The next step is to write the descriptor file:

```yaml
name: Example
name_short: example
version: 0.1.0
namespace: com.cytomine.dummy.example
$schema: https://cytomine.com/schema-store/tasks/task.v0.json

authors:
  - first_name: Cytomine
    last_name: ULiège
    organization: University of Liege
    email: uliege@cytomine.org
    is_contact: true

configuration:
  input_folder: /inputs
  output_folder: /outputs
  image:
    file: /com.cytomine.dummy.example-0.1.0.tar

inputs:
  size:
    display_name: Size
    type:
      id: integer
      gt: 0
    description: Size of the array

outputs:
  sum:
    display_name: Sum
    type: number
    description: The sum of random numbers in the array
```

## Package the task bundle

The final step is to create the task bundle, which consists of the Docker image in a tar archive format and the descriptor file in yaml format.

To save the Docker image as an archive, make sure the name matches `configuration.image.file` in descriptor:

```bash
sudo docker save cytomineuliege/example:0.1.0 -o com.cytomine.dummy.example-0.1.0.tar
```

To create the task bundle:

```bash
zip com.cytomine.dummy.example-0.1.0.zip descriptor.yml com.cytomine.dummy.example-0.1.0.tar
```

At the end of the compression, a zip archive named `com.cytomine.dummy.example-0.1.0.zip` should be generated in the current directory with the Docker image archive and the descriptor.

## Upload the task to Cytomine

Now, you should be able to upload your task bundle to Cytomine either through the Web-UI or directly through the App Engine API.
