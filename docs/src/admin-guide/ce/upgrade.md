---
title: Upgrade
---

# Upgrade Community Edition

Since version `CE2023.1` is the first release in this new family of Community Editions, upgrading is not currently supported.

## Get the latest stable version

First, navigate to the `Cytomine-community-edition` folder, which is the directory where you installed Cytomine. For this example, we will assume this folder is located in your home directory.

```bash
# Get the latest stable version of the project
git fetch origin CE2023.1 --tags --force
git pull origin CE2023.1

# Get the latest version of  the containers
sudo docker pull cytomine/installer
sudo docker run -v $(pwd):/install --user "$(id -u):$(id -g)" --rm -it cytomine/installer:latest deploy -s /install
sudo docker compose pull

# Restart the new containers
sudo docker compose up -d
```
