# Cytomine Contributing Guide

Hey there!
Thanks for helping to make Cytomine even better. Whether you are fixing a bug, improving docs, or adding a new feature, your help means a lot!

This guide will walk you through how to set up your environment, follow our coding style, and submit your contributions smoothly.

Please take a moment to read our [Code of Conduct](https://doc.uliege.cytomine.org/community/code-of-conduct) before diving into the guidelines.

## Understand the basics

If you are new to *pull requests* (often called "PR") or unsure how to submit one, take a moment to read GitHub's [Using Pull Requests](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/about-pull-requests) guide.

If you are not familiar with forks, take a moment to read GitHub's [Fork a repository](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/fork-a-repo) guide.

## Setup the development environment

### Prerequisites

- A **Linux** operating system like [Ubuntu](https://ubuntu.com/), [Debian](https://www.debian.org/), etc.
- [Docker Engine](https://docs.docker.com/get-docker/) (v20.10+ recommended)
- [Docker Compose](https://docs.docker.com/compose/) (v2.0+ recommended)
- [Git](https://git-scm.com/) (v2.0+ recommended)

### Fork the repository

1. On GitHub, navigate to the [cytomine/cytomine](https://github.com/cytomine/cytomine) repository.

2. In the top-right corner of the page, click on **Fork**.

3. Under "**Owner**", select the dropdown menu and click an owner for the forked repository.

    > :bulb: By default, it is your personal account that is set as owner. 

4. Click on **Create fork**.

### Setup the environment

1. Clone the forked repository.

    ```bash
    git clone --recurse-submodules https://github.com/<owner>/cytomine.git
    ```

    Where **owner** is the one you selected in the [Fork the repository](#fork-the-repository) section.

2. Build the Docker images.

    ```bash
    docker compose build
    ```

You are now ready to start contributing to Cytomine.

### Working with containers

Our development environment runs in containers to keep a consistent environment across different setups. Each service run in its own container, and to set up the development environment for a specific service, you can use the Makefile:

```
make start-dev <key-service-1> <key-service-2> ...
```

The available services are:

| Service       | Key Service |
|---------------|-------------|
| app-engine    | ae          |
| cbir          | cbir        |
| core          | core        |
| iam           | iam         |
| pims          | ims         |
| sam           | sam         |
| web-ui        | ui          |

For instance, if you want to work with the web-ui, to setup the development server:

```
make start-dev ui
```

You can also work across several service in development mode:

```
make start-dev ae core ui
```

Which will start the app engine, core, and web-ui in development mode.

> Development mode means it comes with hot reload for the selected service

### Start contributing

We follow the [Conventional Commit](https://www.conventionalcommits.org/en/v1.0.0/) standard.

1. Create a branch on your fork

    ```bash
    git checkout -b feature/my-feature main
    ```

2. Write some code

    Implement your feature, fix, or improvement following the project guidelines.

3. Push your code

    ```bash
    git add .
    git commit -m "feat: add my new feature"
    git push origin feature/my-feature
    ```

4. Create a pull request

    Open a pull request targeting the main branch of [cytomine/cytomine](https://github.com/cytomine/cytomine). Make sure to describe your changes clearly.

    > **Note**: When we merge your PR, we squash all commits into a single commit. The final commit message will be the title of your pull request.

5. Code review and CI

    Our team will review your PR and approve it once it passes all CI checks. Feedback may be provided if changes are needed.

6. Merge and celebrate

    After approval and passing CI, your contribution will be merged. Thank you for helping make Cytomine even better!
