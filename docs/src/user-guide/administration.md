---
title: Administration
---

# Administration

This page provides an overview of the admin panel, including how to access it and a summary of its key functionalities.

## How to Access the Admin Panel

The admin panel is accessible on `https://your-cytomine-instance.org/iam`,
where `your-cytomine-instance.org` is the URL of your Cytomine instance.

The URL will lead to the following page:

![KeyCloak admin page](/images/user-guide/administration/admin-login.png)

To retrieve the admin password, run the following command in the `cytomine-community-edition` folder:

```bash
yq -r '.services.default.iam.constant.KEYCLOAK_ADMIN_PASSWORD' cytomine.yml
```

Use the retrieved password with the `admin` username to access the admin panel.

## Dashboard

The admin panel is organised into several sections, each with specific management capabilities.

![The dashboard of all the Cytomine platform](/images/user-guide/administration/admin-dashboard.png)

## Users

This panel, contains the list of all the accounts on your Cytomine instance with their role (User, Guest, Admin or SuperAdmin).

![The general user management panel](/images/user-guide/administration/admin-users.png)

For each user, you can edit its information, lock the user (it will not be able to connect on Cytomine unless you unlock it) and display some details.
In the details, you can be connected as the selected user on Cytomine, see its projects, annotations and so on.
