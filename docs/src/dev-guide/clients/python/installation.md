---
title: API Client for Python Installation
redirectFrom:
  - /pages/viewpage.action?pageId=12321357
  - /Python-client
---

# {{$frontmatter.title}}

## Requirements

The Cytomine API client for Python library is compatible with Python 3.8+.

The way to install Python depends on your OS or Linux distribution. For Windows and MacOS, get the installers on [the Python.org official website](https://www.python.org/downloads/).

For Linux, `python` is available with your package manager. For example, on Ubuntu and any debian-based linux distributions, you can install Python and its package manager (`pip`) with:

```bash
sudo apt-get install python3 python3-pip
```

## Install as a package

To install the package using `pip`:

```bash
pip install cytomine-python-client
```

The Cytomine API client for the latest release is now installed. See [API Client for Python usage](./usage.md) to getting started.

## Install from source

### Install dependencies

The Cytomine API client for Python requires some other Python libraries that need to be installed before the client. To install these dependencies, run:

```bash
pip install requests requests-toolbelt cachecontrol six future shapely numpy opencv-python-headless
```

### Download the source code

1. Go to the [Github repository](https://github.com/cytomine/Cytomine-python-client/releases/latest) for the Cytomine API client library to get the latest release
2. On this page, click **Source code** and download the archive
3. Extract the downloaded `Cytomine-python-client.zip` archive
4. Open the `Cytomine-python-client` folder

### Install the Cytomine Python client

In the `Cytomine-python-client` folder, run:

```bash
pip install .
```

The Cytomine API client for Python last release is now installed. See [API Client for Python usage](./usage.md) to getting started.
