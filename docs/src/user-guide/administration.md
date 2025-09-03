---
title: Administration
---

# Administration

This page provides an overview of the admin panel, including how to access it and a summary of its key functionalities.

## How to Access the Admin Panel

The admin panel is accessible on `https://your-cytomine-instance.org/iam`,
where `your-cytomine-instance.org` is the URL of your Cytomine instance.

The URL will lead to the following page:

![KeyCloak login page](/images/user-guide/administration/admin-login.png)

To retrieve the admin password, run the following command in the `cytomine-community-edition` folder:

```bash
yq -r '.services.default.iam.constant.KEYCLOAK_ADMIN_PASSWORD' cytomine.yml
```

Use the retrieved password with the `admin` username to access the admin panel.

## Dashboard

The admin panel is organised into several sections, each with specific management capabilities.

![KeyCloak dashboard page](/images/user-guide/administration/admin-dashboard.png)

## Users

This panel is for managing user accounts within Cytomine.

![KeyCloak user page](/images/user-guide/administration/admin-users.png)

In this panel, you are able to:

- Create, view, edit, or delete user accounts.
- Reset user passwords, set temporary passwords, and manage other credentials.
- Assign roles to users to control their access to different resources.
