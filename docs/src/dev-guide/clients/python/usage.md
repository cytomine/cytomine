---
title: API Client for Python Usage
sidebarDepth: 2
redirectFrom:
  - /display/ALGODOC/Data+access+using+Python+client
---

# {{$frontmatter.title}}

The API Client for Python is a library that eases the interaction with Cytomine through its HTTP API. The library encapsulates all the technical details so that you can manipulate Cytomine resources as regular Python objects.

If you are not yet familiar with Cytomine HTTP API, have a look at [Interact with Cytomine](/dev-guide/api/README.md) guide.

## Authentication

First at all, you need 3 parameters to connect to a Cytomine instance :

1. The Cytomine server URL (example: <https://demo.cytomine.be>)
2. Your public key
3. Your private key

These keys are personal and must never be shared because they are linked to your Cytomine account. To retrieve these keys, go to the Cytomine graphical interface. In the Cytomine bar, click **`your-username`**, then **Account** in the dropdown. Your keys are at the end of the page.

In the following example, we log in to the Cytomine server (fictive `https://mycytomine.com`) with a fictive set of keys, and retrieve the information about the current user.

```python
from cytomine import Cytomine
from cytomine.models import CurrentUser

host = 'https://mycytomine.com'
public_key = 'AAA'
private_key = 'ZZZ'

with Cytomine(host, public_key, private_key) as cytomine:
    # We are connected !
    me = CurrentUser().fetch()

    # It will print your username, that has been retrieved from Cytomine.
    print(me.username)
```

::: danger
For the sake of this introduction, the set of keys has been hard-coded into the script. **Do not hard code your keys** into your scripts. As they are personal and has the same value as a password, they should be externalized from your scripts.
:::

### Externalize credentials

A **strongly recommended** good practice is to externalize your credentials (Cytomine server URL and set of keys) and pass them to your scripts as arguments.

The Python standard library provides [`argparse`](https://docs.python.org/3/howto/argparse.html) to deal with command-line parameters. We will use it in the next examples, but you are free to use another one.

Taking back the last example, we log in again to the Cytomine server and retrieve the information about the current user, but this time, the credentials are not written into the code.

```python
import sys
from argparse import ArgumentParser
from cytomine import Cytomine
from cytomine.models import CurrentUser

parser = ArgumentParser(prog="Cytomine Python client example")
parser.add_argument('--host', required=True, help="The Cytomine host")
parser.add_argument('--public_key', required=True, help="The Cytomine public key")
parser.add_argument('--private_key', required=True, help="The Cytomine private key")
params, _ = parser.parse_known_args(sys.argv[1:])

with Cytomine(params.host, params.public_key, params.private_key) as cytomine:
   # We are connected !
    me = CurrentUser().fetch()

    # It will print your username, that has been retrieved from Cytomine.
    print(me.username)
```

We save this file as `test.py`.
Run it with the command:

```bash
python test.py --host https://mycytomine.com --public_key AAA --private_key ZZZ
```

where you have substituted `https//myctomine.com`, `AAA` and `ZZZ` with your actual credentials.
As a result, your Cytomine username will be printed on the terminal.

## Models

A Cytomine **resource** (such as a project _STUDY-01_ or an image _cell01.tiff_) is an instance of a **domain** (in the examples: a project, an image).

In the Cytomine API client for Python, **a resource is a Python object** which is an instance of **a model class** describing its domain. Each object (thus an instance of a model):

- has a set of attributes corresponding to resource attributes
- is managed itself through `fetch()`, `save()`, `update()` and `delete()` methods that communicate with the Cytomine server
- has some utilities to manage HTTP and JSON technical details

Examples of models are `Project`, `ImageInstance`, `Annotation`, `Term`, `User`, `CurrentUser`, etc.

### Fetch a resource

To get data from server, we first need to fetch the resource. Indeed, when you create a new object (such as `myproject = Project()`), the object (here `myproject`) is empty. You need to populate this object with data coming from Cytomine.

::: tip
The next sections show examples to fetch either a project or an image. The sections are very similar: the API client library is indeed very systematic.
:::

#### Example: fetch a project

Supposing the Cytomine server has a project called _STUDY-01_ with an identifier (ID) 42, the project resource is fetched in Python with:

```python
from cytomine.models import Project

# ... Assume we are connected

myproject = Project().fetch(id=42)

# Will print STUDY-01
print(myproject.name)
```

Behind the scene, the API client library has sent a `GET /api/project/42.json` request to the Cytomine server and populated `myproject` with the response content.

Only the project meta information have been retrieved (such as name, ontology identifier, settings, ...). You need to perform other `fetch()` calls on other models to get the project images, annotations, project members, etc.

#### Example: fetch an image

Supposing the Cytomine server has a project with an image called _cells01.tiff_ with an identifier (ID) 13, the image resource is fetched in Python with:

```python
from cytomine.models import ImageInstance

# ... Assume we are connected

myimage = ImageInstance().fetch(id=13)

# Will print cells01.tiff
print(myimage.instanceFilename)
```

Behind the scene, the API client library has sent a `GET /api/imageinstance/13.json` request to the Cytomine server and populated `myimage` with the response content.

Only the image meta information have been retrieved (such as name, width, height, resolution, magnification, ...). You need to perform other `fetch()` calls on other models to get the annotations, an image thumbnail or window, etc.

### Add a resource

You can create data from your Python code and save them to Cytomine. Starting from an empty instance of model, fill in attributes and then save this new resource to the Cytomine server with the `save()` method.

We show here an example to create a new project, but the procedure is again very similar for other type of resources.

#### Example: save a new project

We will first create an empty `Project` object and then populate its attributes.

```python
my_new_project = Project()
my_new_project.name = "My awesome new project"
my_new_project.isRestricted = True
my_new_project.save()
```

Your new project is now saved to Cytomine ! This new project is also available in the graphical interface in the list of your projects (You might need to refresh the page).

Behind the scene, the API client library has sent a `POST /api/project.json` request to the Cytomine server with a JSON request body with the new attributes.

You can use inline constructor to reduce code length:

```python
my_new_project = Project("my project name", isRestricted=True).save()
```

It has exactly the same effect.

### Update a resource

You can update existing Cytomine data from your Python code and make these changes persistent to Cytomine. Starting from a fetched instance of a model, update the attributes you want to change and save these changes to the Cytomine server with the `update()` method.

We show here an example to update the name of a project, but the procedure is again very similar for other type of resources and attributes.

#### Example: update a project

```python
myproject = Project().fetch(id=42)

myproject.name = "A new name"
myproject.update()
```

The project has a new name.

Behind the scene, the API client library has sent a `PUT /api/project/42.json` request to the Cytomine server with a JSON request body with the updated attributes.

You can use inline constructor to reduce code length:

```python
myproject = Project().fetch(id=42)
myproject.update(name="A new name")
```

It has exactly the same effect.

### Delete a resource

It is possible to delete Cytomine resources from the API client library. Deleting resources can have unintended side effects and implies deletion of all dependent resources.

From a fetched resource, call the `delete()` method.

We show here an example to delete a project (and thus all its images, annotations, etc.), but the procedure is again very similar for other type of resources.

#### Example: delete a project

```python
badproject = Project().fetch(id=42)
badproject.delete()
```

The project and all dependent resources are deleted.

Behind the scene, the API client library has sent a `DELETE /api/project/42.json` request to the Cytomine server.

You can delete resources without fetching by directly providing ID:

```python
Project().delete(id=42)
```

### Fetch embedded resources

Next to the four fetch/add/update/delete actions, some resources provides supplementary actions. These actions are encapsulated in additional model methods.

To download an image file on your computer, the `ImageInstance` model has an additional `download()` method. To download the image with ID 13 on your computer at location `/tmp/myimage13.tiff`, use:

```python
image = ImageInstance().fetch(13)
image.download("/tmp/myimage13.tiff")
```

To get a 256-pixel wide image thumbnail, use the additional `ImageInstance.dump()` method:

```python
image = ImageInstance().fetch(13)
image.dump("/tmp/thumb.png", max_size=256)
```

Refer to the [Cytomine API client for Python reference](./reference.md) to get existing supplementary actions and their parameters.

## Collections

A collection is a list of objects that have the same model. All models have a collection equivalent. For example, the `ProjectCollection` is a collection of `Project`, the `ImageInstanceCollection` is a collection of `ImageInstance`, etc.

To retrieve all resources from a collection, use the `fetch()` method on the collection.
Here, we get the list of all projects we have access to:

```python
projects = ProjectCollection().fetch()
print(projects)
for project in projects:
    print(project)
    print(project.name)
```

Behind the scene, the API client library has sent a `GET /api/project.json` request to the Cytomine server and populated `projects` with the response content.

The collections behave like regular Python lists:

```python
projects = ProjectCollection().fetch()

# Print the number of project in the collection
print(len(projects))

# Print the first project in the collection
print(projects[0])

# Print a list with the project names in the collection
print([project.name for project in projects])
```

### Filtering

Sometimes, we are not interested in getting all the projects we have access to, but only a subset of it depending on some filters.

The list of available filters is returned by `mycollection.filters()`. For example, `projects.filters()` returns these elements:

- `None` - the collection of projects can be fetched without any filter
- `user` - to get a collection of projects that a given user is member of
- `ontology` - to get a collection of projects that share the given ontology
- `software` - to get a collection of projects where the given software is installed.

Supposing the Cytomine server has an ontology with ID 100, the list of project that uses this ontology is retrieved with the `fetch_with_filter()` method:

```python
projects = ProjectCollection().fetch_with_filter("ontology", 100)
```

Behind the scene, the API client library has sent a `GET /api/ontology/100/project.json` request to the Cytomine server and populated `projects` with the response content.

#### Query modifiers

The behavior of collection fetching can sometimes be modified with some query parameters. When they exist, these query modifiers are attributes of the `Collection` class. For example, the `online` query modifier can further filter a collection to get only online users:

| Request                                       | Python code                                                   | HTTP API request                            |
|-----------------------------------------------|---------------------------------------------------------------|---------------------------------------------|
| Fetch list of users you have access to        | `UserCollection().fetch()`                                    | `GET /api/user.json`                        |
| Fetch list of online users you have access to | `UserCollection(online=True).fetch()`                         | `GET /api/user.json?online=true`            |
| Fetch list of users in a project              | `UserCollection().fetch_with_filter("project", 42)`           | `GET /api/project/42/user.json`             |
| Fetch list of online users in a project       | `UserCollection(online=True).fetch_with_filter("project, 42)` | `GET /api/project/42/user.json?online=true` |

## Special case: Annotations

### Search annotations

Annotations are a key features in Cytomine and can be filtered in many ways. You might be interested in getting a listing of all annotations in a subset of images, created by a subset of project members and only associated to a specific term. Such queries are frequent with annotation resources.

**`AnnotationCollection` filtering differs from other collection.** With `AnnotationCollection`, all filters are [query modifiers](#query-modifiers) and falls into 2 categories:

1. Regular query filters that **filter the collection** such as `project`, `user`, `users`, `image`, `images`, `term`, etc.
2. Query filters that **show or hide some annotation attributes**, for each annotation in the collection. It can reduce the amount of data transferred between the Cytomine server and your computer, as it can be very large when there are many annotations. Such modifiers start with `show`: `showTerm`, `showWKT`, etc.

The following example get all annotations:

- in the project with ID 42
- created by project members with ID 7 and 8
- associated to the term 99
  and fetch the `term` and `location` attributes.

```python
annotations = AnnotationCollection()
annotations.project = 42
annotations.users = [7, 8]
annotations.term = 99
annotations.showTerm = True
annotations.showWKT = True
annotations.fetch()
```

Behind the scene, the API client library has sent a `GET /api/annotation.json` request to the Cytomine server and populated `annotations` with the response content.

The last code snippet can be written in a more concise way:

```python
annotations = AnnotationCollection(project=42, users=[7, 8], term=99, showTerm=True, showWKT=True).fetch()
```

### Multiple adds in one request

When you have a lot of new annotations to add, it is recommended to send these multiple new annotations in one request for all instead of one by one (default behavior). The following example saves annotations one by one and is inefficient:

```python
for geometry in geometry_generator:
    annot = Annotation(location=geometry, id_image=13, id_project=42).save()
```

It can be rewritten efficiently:

```python
annots = AnnotationCollection()
for geometry in geometry_generator:
    annot = Annotation(location=geometry, id_image=13, id_project=42)
    annots.append(annot)
annots.save()
```

## Verbose mode

The `Cytomine` class exposes a `verbose` parameter allows to change the verbosity level. It has to be an integer and follows [Python standard logging library](https://docs.python.org/3/library/logging.html). Acceptable verbosity level are:
| `logging`                | Level | Meaning                                                                   |
|--------------------------|-------|---------------------------------------------------------------------------|
| `logging.CRITICAL`       | 50    | Only critical log messages                                                |
| `logging.ERROR`          | 40    | Log error messages (such as failed requests) and above                    |
| `logging.WARNING`        | 30    | Log warning messages (such as usage of deprecated methods) and above      |
| `logging.INFO` (default) | 20    | Log successful messages (after a fetch, save, update or delete) and above |
| `logging.DEBUG`          | 10    | Log all HTTP requests and responses (very large outputs) and above        |

```python
import logging
from cytomine import Cytomine
with Cytomine(host, public_key, private_key, verbose=logging.INFO) as cytomine:
    print(cytomine.current_user)
```
