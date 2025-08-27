---
title: Uninstall Cytomine Community Edition
---

# {{$frontmatter.title}}

We are sorry to know that Cytomine did not meet your expectations. Please <a :href="'mailto:'+$cytomine.email+'?subject=Uninstall%20Cytomine'">let us know the reason</a>.

::: danger
The following procedure will remove **all** Cytomine software, **including databases and images**
:::

## Step 0 - (Optional) Backup your data

Before uninstalling Cytomine, please consider to [back up your data](backup.md), especially if you plan to reuse it in another Cytomine instance or a different application. Ensure that your backup is stored safely by relocating it to a secure location.

## Step 1 - Stop and delete the Cytomine system Docker containers

First, navigate to the `Cytomine-community-edition` folder, which is the directory where Cytomine was installed. For this example, we will assume that this folder is located in your home directory.

In this `Cytomine-community-edition` folder, you must first stop all the Docker containers by running the following command:

```bash
sudo docker compose stop
```

Note that at this stage, your data and images are still intact, allowing you to revert the uninstallation and restore your Cytomine instance by running the following command:

```bash
sudo docker compose up -d
```

## Step 2 - Delete the Cytomine databases and data, and uploaded images

In this second step, we will delete the Docker volume that contains the data:
::: danger
This is **irreversible!**

If you plan to reuse your data later, make sure to back it up before proceeding.
:::

```bash
# Clean docker volumes
sudo docker compose down -v

# Remove data folder
sudo rm -rf ./data/*
```

## Step 3 - Delete your Cytomine Community Edition folder

If you have followed the installation procedure, all the configuration files for your Cytomine instance are located in the `Cytomine-community-edition` folder, which you can now delete.

## Step 4 - Delete the Cytomine Docker images

You can now delete the Docker images that were downloaded from DockerHub during the installation process.

```bash
sudo docker image prune
```
