---
title: References
---

# {{ $frontmatter.title }}

This section provides a list of example Cytomine tasks you can use as a starting point or for testing.

## Dummy Tasks

These are tasks to showcase different parameter types used by the app engine.

::: tip How to use
All dummy tasks live in the [cytomine/dummy-tasks](https://github.com/cytomine/dummy-tasks) repository. Download the `zip` archive of the task you want to test directly from GitHub, then upload it to your Cytomine instance via the app engine interface.
:::

| Task | Parameter Type | Repository |
|------|---------------|------------|
| Array Identity | Integer array | [identity integer array](https://github.com/cytomine/dummy-tasks/tree/main/identity%20integer%20array) |
| Boolean Identity | Boolean | [identity boolean](https://github.com/cytomine/dummy-tasks/tree/main/identity%20boolean) |
| Datetime Identity | Datetime | [identity datetime](https://github.com/cytomine/dummy-tasks/tree/main/identity%20datetime) |
| Enum Identity | Enum | [identity enum](https://github.com/cytomine/dummy-tasks/tree/main/identity%20enum) |
| File Identity | File | [identity file](https://github.com/cytomine/dummy-tasks/tree/main/identity%20file) |
| Geometry Identity | Geometry | [identity geometry](https://github.com/cytomine/dummy-tasks/tree/main/identity%20geometry) |
| Image Identity | Image | [identity image](https://github.com/cytomine/dummy-tasks/tree/main/identity%20image) |
| WSIDICOM Image Identity | WSI DICOM image | [identity wsi dicom image](https://github.com/cytomine/dummy-tasks/tree/main/identity%20wsi%20dicom%20image%20) |
| WSIDICOM Image Array Identity | WSI DICOM image array | [identity wsi dicom image array](https://github.com/cytomine/dummy-tasks/tree/main/identity%20wsi%20dicom%20image%20array) |
| Integer Identity | Integer | [identity integer](https://github.com/cytomine/dummy-tasks/tree/main/identity%20integer) |
| Number Identity | Number (float) | [identity number](https://github.com/cytomine/dummy-tasks/tree/main/identity%20number) |
| String Identity | String | [identity string](https://github.com/cytomine/dummy-tasks/tree/main/identity%20string) |

## Other Tasks

These are real-world task integrations combining algorithms and models for image analysis workflows. They are good references for understanding how to structure a non-trivial Cytomine task.

### VALIS experiment

Repository: [maxime915/app_engine_valis_experiment](https://github.com/maxime915/app_engine_valis_experiment)

Performs image registration using the [VALIS](https://github.com/MathOnco/valis) library.
It aligns multiple whole-slide images onto a common coordinate space, enabling cross-slide comparison and analysis.

### Stardist task

Repository: [cytomine/task-stardist](https://github.com/cytomine/task-stardist)

Detects and segments cell nuclei using the [StarDist](https://github.com/stardist/stardist) deep learning model developed by Uwe Schmidt et al.
StarDist represents nuclei as star-convex polygons, making it well-suited for densely packed nuclei in fluorescence and H&E images.

### TIA Centre Cytomine tasks

Repository: [TissueImageAnalytics/cytomine-app](https://github.com/TissueImageAnalytics/cytomine-app)

A collection of Cytomine tasks developed by the [TIA Centre](https://warwick.ac.uk/fac/cross_fac/tia/), University of Warwick, wrapping pre-trained models from [TIAToolbox](https://github.com/TissueImageAnalytics/tiatoolbox). The repository currently contains two tasks:

- **cytomine-hovernet**: nucleus instance segmentation and classification using [HoVer-Net](https://github.com/vqdang/hover_net), trained on the PanNuke dataset.
- **cytomine-kongnet**: nucleus detection using KongNet, trained on the [MONKEY Challenge](https://monkey.grand-challenge.org/) dataset.
