---
title: Architecture
---

# Architecture

## List of containers

Cytomine Community Edition consists of a collection of services, each running in its own Docker container, as listed below:

| Name       |                     Required                      | Goal                                               |
| ---------- | :-----------------------------------------------: | -------------------------------------------------- |
| Nginx      | <Badge text="Yes" type="tip" vertical="middle"/>  | Main HTTP(S) proxy dispatching incoming requests.  |
| Core       | <Badge text="Yes" type="tip" vertical="middle"/>  | Main Cytomine server. Provide the REST API.        |
| Postgres   | <Badge text="Yes" type="tip" vertical="middle"/>  | Main database. Store most of data.                 |
| MongoDB    | <Badge text="Yes" type="tip" vertical="middle"/>  | Secondary database. Store activity data.           |
| PIMS       | <Badge text="Yes" type="tip" vertical="middle"/>  | Main image server.                                 |
| PIMS-cache | <Badge text="Yes" type="tip" vertical="middle"/>  | Fast cache for images.                             |
| App Engine | <Badge text="No" type="error" vertical="middle"/> | Service to execute containerised apps.             |
| Web UI     | <Badge text="No" type="error" vertical="middle"/> | Web graphical user interface.                      |

## Global architecture

![Cytomine Community Edition Architecture](/images/admin-guide/ce/ce-2025-1-overview.svg)
