---
title: Backup and restore
---

<!-- markdownlint-disable MD024 -->

# {{ $frontmatter.title }}

## Backup

Non-volatile data are:

1. postgres database
2. mongo database
3. images files
4. Cytomine configuration

All non-volatile data are stored in the `cytomine.yml` file (root folder) and the `data/` folder, so having a copy of that folder should allow you restore your Cytomine instance. But please consider the fact that the `images/` folder might grow quick as images themselves can be quite big. It might be relevant to back up them to a separate target.

::: warning
In the following instructions, the backups will be stored in the same machine than the backed up database. It is your responsibility to copy the backups to an appropriate mass storage device.
:::

### Postgis database

The `postgis` service produces every 24 hours an archive in its mounted volume.

```bash
$ sudo ls -al data/postgis/data/backup
total 48
drwxr-xr-x  2   70 root  4096 sep 29 13:44 .
drwx------ 20   70 root  4096 sep 29 13:30 ..
-rw-r--r--  1   70 root 11043 sep 29 13:44 backup.log
-rw-r--r--  1 root root 27115 sep 29 13:44 cytomine_postgis_backup_Fri.tar.gz
```

It keeps the last 7 backup archives before overwriting the oldest one.

It can be further invoked from the host running the container:

```bash
docker exec postgis backup
```

### MongoDB

The `mongo` service produces an archive every 24 hours in its mounted volume.

```bash
$ sudo ls -al data/mongo/backup
total 48
drwxr-xr-x  2   70 root  4096 sep 29 13:44 .
drwx------ 20   70 root  4096 sep 29 13:30 ..
-rw-r--r--  1   70 root 11043 sep 29 13:44 backup.log
-rw-r--r--  1 root root 27115 sep 29 13:44 cytomine_mongo_backup_Fri.tar.gz
```

It keeps the last 7 backup archives before overwriting the oldest one.

It can be further invoked from the host running the container:

```bash
docker exec mongo backup
```

### Images files

This is a stateless folder mounted by default from `data/images` on your host.  
Please back up the whole folder.  
To restore, the folder can be simply be copied from a backup.

### Cytomine configuration

It is really important to keep a copy of the `cytomine.yml` file (root folder) as it contains sensitive data like passwords.
This file is needed to restore you databases, along with all Cytomine configuration.

## Restore

Non-volatile data are:

1. postgres database
2. mongo database
3. image database
4. Cytomine configuration

::: warning  
Postgres and MongoDB, should be restored at the same time, to the same point back in time. If you restore a backup from two days ago, both Postgres and Mongo should be restored from two days ago.  
:::

### Postgis database

::: danger  
Data loss can occur, read the following carefully.  
:::

To restore the database from a backup, follow those steps:

1. In the volume folder of the `postgis` container `data/postgis/data/backup/` rename the backup archive you want to restore into `restore.tar.gz`:

   ```bash
   Cytomine-community-edition$ sudo mv data/postgis/data/backup/cytomine_postgis_backup_<pick a day!>.tar.gz data/postgis/data/backup/restore.tar.gz
   ```

2. Run the restore script:

   ```bash
   Cytomine-community-edition$ docker exec postgis restore
   ```

3. Also proceed to the Mongo restore of the same day.
4. Restart Cytomine

   ```bash
   sudo docker compose down
   sudo docker compose up -d
   ```

### Mongo database

::: danger  
Data loss can occur, read the following carefully.  
:::

To restore the database from a backup, follow those steps:

1. In the volume folder of the `mongo` container `data/mongo/data/backup/` rename the backup archive you want to restore into `restore.tar.gz`:

   ```bash
   Cytomine-community-edition$ sudo mv data/mongo/data/backup/cytomine_mongo_backup_<pick a day!>.tar.gz data/mongo/backup/restore.tar.gz
   ```

2. Run the restore script:

   ```bash
   Cytomine-community-edition$ docker exec mongo restore
   ```

3. Also proceed to the Postgres restore of the same day.
4. Restart Cytomine

   ```bash
   sudo docker compose down
   sudo docker compose up -d
   ```

### Images files

To restore images, the folder `data/images` can be simply be copied from a backup.  
Please double-check the file permissions and ownership.

### Cytomine configuration

Restore the `cytomine.yml` file to the root directory and run the ['update configuration' procedure](/admin-guide/ce/installation#update-configuration).
