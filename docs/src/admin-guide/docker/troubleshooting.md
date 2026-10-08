---
title: Troubleshooting
---

# Troubleshooting Cytomine

::: tip
If your issue does not appear in the list, please provide a detailed description of the problem [in our ticket system on Github.](https://github.com/cytomine/cytomine/issues)
:::

## Issue with K3s

An issue may arise with K3s configuration file, you will have to remove it:
```bash
cd cytomine
rm -rf .kube
```

And rerun the `docker compose up -d` command.

## Issue with postgresql-15 migration

If you stumble on this issue:

```
iam-db-1 | PostgreSQL Database directory appears to contain a database; Skipping initialization
iam-db-1 |
iam-db-1 | 2026-05-07 09:03:41.062 UTC [1] FATAL: database files are incompatible with server
iam-db-1 | 2026-05-07 09:03:41.062 UTC [1] DETAIL: The data directory was initialized by PostgreSQL version 14, which is not compatible with this version 15.17 (Debian 15.17-1.pgdg13+1).
```

The best option is probably to erase your data.
But you can also dump and restore it.
Follow this link's instructions: [https://stackoverflow.com/questions/78782410/docker-the-data-directory-was-initialized-by-postgresql-version-14-which-is-no/78782530#78782530](https://stackoverflow.com/questions/78782410/docker-the-data-directory-was-initialized-by-postgresql-version-14-which-is-no/78782530#78782530)
