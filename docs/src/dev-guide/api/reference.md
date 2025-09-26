---
api: true
sidebar: true
title: REST API reference
sidebarDepth: 0
---

::: warning
This API reference is under construction. Some endpoint specifications could be incorrect.
:::

All interactions with the Cytomine server are performed through an HTTP API. It makes the link between the server side and a Cytomine client, such as the graphical web interface or a program using one of our API client libraries (for [Python](/dev-guide/clients/python/usage.md)).

This API reference provides the specification for all endpoints available in Cytomine. The different endpoints are grouped into services in the left sidebar. For a more in-depth introduction, see [Interact with Cytomine](/dev-guide/api/README.md).

## API endpoint conventions

For most of the services, a resource is created with a `POST /api/resource.json` request with a request body. The response provides the Cytomine identifier for that new resource.

To retrieve this resource, send a `GET /api/resource/{id}.json` request where `{id}` is the Cytomine resource identifier.

To update a resource, send a `PUT /api/resource/{id}.json` request with a request body where `{id}` is the Cytomine resource identifier.

The resource can be deleted with a `DELETE /api/resource/{id}.json` request where `{id}` is the Cytomine resource identifier.

It is possible to retrieve a list of resources with the same domain using a `GET /api/resource.json` request. These listing can often be filtered with path and/or query parameters.

## API services

| Service                           | Description                                               |
| --------------------------------- | --------------------------------------------------------- |
| Image \| Image instance           | Manage images in a project                                |
| Image \| Slice instance           | Manage slices of an image in a project                    |
| Image \| Image server             | Manage image servers (IMS)                                |
| Image \| Abstract image           | Manage images in a storage                                |
| Image \| Abstract slice           | Manage slices of an image in a storage                    |
| Image \| Companion file           | Manage companion files of an image in a storage           |
| Storage                           | Manage storages (virtual disk space)                      |
| Storage \| Member                 | Manage storage members                                    |
| Storage \| Uploaded file          | Manage uploaded files in a storage                        |
| Annotation                        | Manage annotations regardless its type                    |
| Annotation \| User annot.         | Manage annotations created by human users                 |
| Annotation \| Reviewed annot.     | Manage reviewed annotations validated by human users      |
| Annotation \| Annotation index    | Manage annotation indexes                                 |
| Annotation \| Associated term     | Manage terms associated to annotations                    |
| Annotation \| Associated track    | Manage tracks associated to annotations                   |
| Ontology                          | Manage ontologies                                         |
| Ontology \| Term                  | Manage terms in an ontology                               |
| Ontology \| Relation              | Manage relations                                          |
| Ontology \| Relation term         | Manage relation between terms in ontology                 |
| Track                             | Manage tracks in an image                                 |
| Project                           | Manage projects                                           |
| Project \| Command                | Manage command history on a project                       |
| Project \| Member                 | Manage project members                                    |
| Project \| Default layer          | Manage project default layers                             |
| Project \| Representative user    | Manage project representative users                       |
| Project \| Associated software    | Manage software installed in a project                    |
| User                              | Manage users                                              |
| User \| Role                      | Manage app-roles                                          |
| User \| Associated role           | Manage app-roles associated to users                      |
| Activity \| Annotation action     | Manage actions performed on annotations                   |
| Activity \| Image consultation    | Manage consultation records on images                     |
| Activity \| Project connection    | Manage connection records on projects                     |
| Activity \| User position         | Manage user position records on images                    |
| Metadata \| Property              | Manage properties (key-pair value) metadata               |
| Metadata \| Attached file         | Manage attached files metadata                            |
| Metadata \| Rich-text description | Manage rich-text description metadata                     |
| Metadata \| Tag                   | Manage tags metadata                                      |
| Metadata \| Tag association       | Manage associations between a tag and a domain            |
