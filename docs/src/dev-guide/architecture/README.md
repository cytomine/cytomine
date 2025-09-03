---
title: Architecture
---

# {{ $frontmatter.title }}

In this section, you will learn how to extend the functionality of the Cytomine platform or develop new features.

If you don't know Cytomine yet, follow the [Getting Started](/user-guide/getting-started.md) page in the user guide to have a tour and discover the main concepts of Cytomine.

## List of containers

Cytomine consists of a collection of services, each running in its own Docker container, as listed below:

| Name       |                     Required                      | Goal                                               |
| ---------- | :-----------------------------------------------: | -------------------------------------------------- |
| Nginx      | <Badge text="Yes" type="tip" vertical="middle"/>  | Main HTTP(S) proxy dispatching incoming requests.  |
| Core       | <Badge text="Yes" type="tip" vertical="middle"/>  | Main Cytomine server. Provide the REST API.        |
| PostGIS    | <Badge text="Yes" type="tip" vertical="middle"/>  | Main database. Store most of data.                 |
| MongoDB    | <Badge text="Yes" type="tip" vertical="middle"/>  | Secondary database. Store activity data.           |
| PIMS       | <Badge text="Yes" type="tip" vertical="middle"/>  | Image management server.                           |
| PIMS-cache | <Badge text="Yes" type="tip" vertical="middle"/>  | Fast cache for images.                             |
| CBIR       | <Badge text="Yes" type="tip" vertical="middle"/>  | Service for similar search.                        |
| IAM        | <Badge text="Yes" type="tip" vertical="middle"/>  | Service for authentication.                        |
| App Engine | <Badge text="No" type="error" vertical="middle"/> | Service to execute containerised apps.             |
| Web UI     | <Badge text="No" type="error" vertical="middle"/> | Web graphical user interface.                      |
| SAM        | <Badge text="No" type="error" vertical="middle"/> | Annotation refinement module.                      |

- [Core](https://github.com/cytomine/cytomine/tree/main/core). This service contains the programming logic, object representations, dependencies, and access permissions. The Core service is also responsible for connecting to the databases. It is developed using the [Spring Boot](https://spring.io/projects/spring-boot) framework.
- [PIMS](https://github.com/cytomine/cytomine/tree/main/pims). This service is the image management system and is connected to the storage disk. Imported images on Cytomine are handled by this service. It is also responsible to return the tiles displayed in the viewer when you browse an image as well as the thumbnails and annotation crops. It is developed in Python using the [FastAPI](https://fastapi.tiangolo.com/) framework.
- [Web-UI](https://github.com/cytomine/cytomine/tree/main/web-ui). This service is our officially supported front-end. It is a Web User Graphical Interface developed in [Vue 2](https://vuejs.org/). All graphical services you see during your Cytomine browser navigation are implemented here.
- [App Engine](https://github.com/cytomine/cytomine/tree/main/app-engine). This service is an execution system that allows developers to integrate their own algorithms or applications within the Cytomine platform. It is developed using the [Spring Boot](https://spring.io/projects/spring-boot) framework.
- The external clients API libraries (currently [Python](https://github.com/cytomine/Cytomine-python-client/)). As Cytomine is a RESTful platform, it is possible to interact with a Cytomine instance with HTTP(S) requests and without using a graphical interface. Clients are libraries developed to help you to integrate interactions with Cytomine in your scripts or your applications.

![Cytomine Community Edition 2025.1](/images/admin-guide/ce/cytomine-2025.1-overview.svg)
