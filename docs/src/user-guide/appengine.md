---
title: App Engine
---

# App Engine

## Concepts

The App Engine allows to execute Tasks (see [Task dev guide](../dev-guide/algorithms/task/) for information about how developers create Tasks compatible with Cytomine App Engine). Tasks could be AI apps or simpler scripts to manipulate data and generate results.


## Applications

The Applications Tab shows available Tasks on your Cytomine instance and allows to add new Tasks (provided they are packaged to be compatible, see [Task dev guide](../dev-guide/algorithms/task/)) or delete useless Tasks.

![Applications page](/images/user-guide/appengine/applications.png)


## Run a Task

Tasks can be run directly in the image viewer through the App Engine panel (bottom of the screen):

![Image viewer](/images/user-guide/appengine/imageviewer.png)

The panel allows to select a Task (left), parameter values for this specific task, run the Task, and see logs of recent runs:

![Image viewer Run a Task](/images/user-guide/appengine/run-task.png)

If a Task takes as inputs images or geometries, a dialog allows the user to select image(s) or geometries:

![Image viewer Run a Task](/images/user-guide/appengine/select-geometry.png)

Task outputs can be seen and downloaded in the log of recent runs, or if a Task creates geometries, they are visible in the annotation layers.