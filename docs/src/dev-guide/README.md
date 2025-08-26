---
title: Cytomine is RESTful
---

# Introduction

Data are central in Cytomine and the Cytomine web graphical user interface lets you explore (whole-slide) images, annotations and any metadata associated to them. If you're not yet familiar with Cytomine, have a look at the [Cytomine User Guide](/user-guide/) to have a guided tour of this graphical interface and discover the main Cytomine features.

However, sometimes a GUI is not enough as you might want to **be able to easily collect, manipulate or even inject data** into the platform. Here we are: all data available from the graphical interface can be manipulated programmatically from your computer using our APIs.

## Cytomine

Cytomine is a [RESTful](https://en.wikipedia.org/wiki/Representational_state_transfer) application. It means that the data stored and managed by Cytomine can be obtained through **specific URLs**. Contrary to the graphical interface, these URLs only provide relevant information data and are called API endpoints.

These API endpoints let Cytomine users and external tools interact with Cytomine for various purposes:

- Data mining
- Scripting
- Interact with AI algorithms
- Create new graphical interfaces for dedicated purposes
- Etc.

![The Cytomine HTTP API](/api/cytomine-rest-api-simple.svg)

The [Cytomine HTTP API](/dev-guide/api/reference.md) structures and specifies all the endpoints for the whole application. All the endpoints are secured. It means that you need to be authenticated (with some HTTP mechanisms) so that the Cytomine server accept to treat your request. You keep the rights you have in the graphical interface: if you don't have the rights to perform some actions in the graphical interface, you don't have them by using the API neither. The graphical interface actually relies on the HTTP API.

When dealing with an HTTP API, you have to manage yourself the HTTP layer: create the request, authenticate the user using appropriate HTTP header mechanism, send the request to the server, wait for the response, analyse and parse the received response. In the previous example with your first API test, your browser has managed all of these for you.

When you are running a program outside your browser, you have to manage these yourself, which is not very convenient. To ease interaction with Cytomine, we have developed **API client libraries** for multiple programming languages that do the job for you and let you focus on your task.

Whether you are a data scientist, a machine or deep learning developer or a system administrator, you might be interested to learn how to use these libraries to help you in your tasks.

## API client libraries

To ease interaction with Cytomine, the **API client libraries** encapsulate all the technical details relative to the HTTP API so that you can manipulate Cytomine resources without complexity. We have API client libraries for 2 languages:

- the [API client library for Python](/dev-guide/clients/python/usage.md)

Choose the API client library in the language you prefer. The libraries tend to follow the same structure so that you can switch from a language to another without learning a whole new library.
Some libraries embed extra functionalities, such as the API client library for Python which provides helpers for machine and deep learning.

::: tip What if my language is missing?
You can interact with Cytomine in other languages that currently do not have a dedicated client library, but you have to manage yourself the usually encapsulated tasks such as HTTP handling, authentication and response parsing.

However, implement an API client library in another language in a good way to [contribute to the Cytomine Open Source project](/community/how-to-contribute.md).
:::

### Client library structure

We present here the general structure of a Cytomine API client library, but refer to the documentation relative to each library for more details.

The Cytomine API client libraries follows the [Object Oriented paradigm](https://en.wikipedia.org/wiki/Object-oriented_programming). A Cytomine **resource** (such as a project _STUDY-01_ or an image _cell01.tiff_) is an instance of a **domain** (here, the project or the image domain).

In terms of object-oriented programming, **a resource is an object** which is an instance of **a model class** describing its domain. Each object (thus an instance of a model):

- has a set of attributes corresponding to resource attributes
- is managed itself through `fetch()`, `save()`, `update()` and `delete()` methods that communicate with the Cytomine server
- has some utilities to manage HTTP and JSON technical details

Examples of models in Cytomine are `Project`, `ImageInstance`, `Annotation`, `Term`, `User`, `CurrentUser`, etc.

A `Collection` is a representation of a collection of models. It has methods to fetch multiples instances of a model with filters and/or pagination.

## A first script example

1. Choose the API client library in the language you prefer.
2. Install the library following the instructions on dedicated pages (in a new development project or environment, depending on the selected language)
3. When the library is installed, you can use it as any other library into your code.

In this first script, we would like to **count the number of annotations that are associated to a given ontology term in all projects we have access to and that have `P` as first letter in their name**.

<code-group>
<code-block title="Python">

<<< @/code-snippets/first-script-example/count_annotations.py

</code-block>
</code-group>

## What can I do next ?

Learn how to master the API client library for your language with the dedicated guides and examples (for [Python](/dev-guide/clients/python/usage.md)).
