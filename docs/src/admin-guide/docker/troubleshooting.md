---
title: Troubleshooting
---

# Troubleshooting Cytomine

::: tip
If your issue does not appear in the list, please provide a detailed description of the problem [in our ticket system on Github.](https://github.com/cytomine/cytomine/issues)
:::

## Issue with K3s

An issue may arise with K3s configuration file, you will have to remove the configuration file:
```bash
cd cytomine
rm -rf .kube
```

And rerun the `docker compose up -d` command.
