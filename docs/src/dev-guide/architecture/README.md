---
title: Architecture
---

# {{ $frontmatter.title }}

In this section, you will learn how to extend the functionality of the Cytomine platform or develop new features.

If you don't know Cytomine yet, follow the [Getting Started](/user-guide/getting-started.md) page in the user guide to have a tour and discover the main concepts of Cytomine.

The Cytomine project is composed of multiple services, each implemented using different programming languages, which interact with one another to provide the full functionality of the platform. You can contribute to one or more services of the Cytomine platform based on your skills and expertise. Please see the [How to contribute guide](/community/how-to-contribute.md) to explore the various ways of improving Cytomine and contributing effectively.

## Community Edition 2025.1

- [Core](https://github.com/cytomine/Cytomine-core). This service contains the programming logic, object representations, dependencies, and access permissions. The Core service is also responsible for connecting to the databases. It is developed using the [Spring Boot](https://spring.io/projects/spring-boot) framework.
- [PIMS](https://github.com/cytomine/Cytomine-pims). This service is the image management system and is connected to the storage disk. Imported images on Cytomine are handled by this service. It is also responsible to return the tiles displayed in the viewer when you browse an image as well as the thumbnails and annotation crops. It is developed in Python using the [FastAPI](https://fastapi.tiangolo.com/) framework.
- [Web-UI](https://github.com/cytomine/Cytomine-Web-UI). This service is our officially supported front-end. It is a Web User Graphical Interface developed in [Vue 2](https://vuejs.org/). All graphical services you see during your Cytomine browser navigation are implemented here.
- [App Engine](https://github.com/cytomine/Cytomine-app-engine). This service is an execution system that allows developers to integrate their own algorithms or applications within the Cytomine platform. It is developed using the [Spring Boot](https://spring.io/projects/spring-boot) framework.
- The external clients API libraries (currently [Python](https://github.com/cytomine/Cytomine-python-client/)). As Cytomine is a RESTful platform, it is possible to interact with a Cytomine instance with HTTP(S) requests and without using a graphical interface. Clients are libraries developed to help you to integrate interactions with Cytomine in your scripts or your applications.

![Cytomine Community Edition 2025.1](/images/admin-guide/ce/ce-2025-1-overview.svg)
