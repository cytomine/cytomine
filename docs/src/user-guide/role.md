---
title: Roles
---

# Roles

In Cytomine, all users have defined permissions that allow them to do specific actions.  
The permissions in Cytomine are associated to roles that can be related to the platform, or related to each [project](./project.md) the user is member of.

## Platform-wide roles

There are 4 roles that can be given to users to define their permissions at a platform level:

### Roles for active users

- **USER**: The USER role is the standard role for fully active users. A USER can create a project, upload its images to its storage, and add its images in the project it is member of.
- **GUEST**: The GUEST role is the minimum role for active users. A GUEST can be active in the projects it is member of, but cannot create any project, nor upload any images to the platform (it does not have a Storage).

### Roles for administrator users

- **ADMIN**: An ADMIN user can access the administrator panel, can create users accounts, can use account of anyone in the same instance, can lock other users, access to any project, etc. Only trusted users should have the ADMIN role. An ADMIN user must activate its administrator privileges before using them.
- **SUPERADMIN**: A SUPERADMIN is an ADMIN which administrator privileges are activated by default. This role is mainly used by system accounts.

To learn how to create users accounts and how to set their platform-wide role, go to the [administration section dedicated to users](./administration#users).

### Summary

|                                                        | Guest | User | Admin | SuperAdmin |
| -----------------------------------------------------: | :---: | :--: | :---: | :--------: |
|                              Can be active in projects |  Yes  | Yes  |  Yes  |    Yes     |
|                                Can create new projects |  No   | Yes  |  Yes  |    Yes     |
|                                      Can upload images |  No   | Yes  |  Yes  |    Yes     |
|                          Can administrate the platform |  No   |  No  |  Yes  |    Yes     |
|                    Can access to any user account data |  No   |  No  |  Yes  |    Yes     |
| Have the administrator privileges activated by default |  No   |  No  |  No   |    Yes     |

## Project-wide roles

Users in a project are divided into two categories:

- A project **CONTRIBUTOR** is a user which has access to the project, but cannot add images in this project, nor manage the [configuration](./project#configuration) of the project. Allowed actions like doing annotations on the images, are determined by the [editing mode of the project](./project#editing-mode).
- A project **MANAGER** is a project contributor with extended rights. It can add images and manages the project, i.e. has rights to change the project [configuration](./project#configuration), to add new users to a project (as contributor or manager) or to delete the project itself. The user who creates a project is a project manager by default.

To learn how to add users to a project, and define their project-wide role, go to the [project](./project.md) section.

|                                    |                                                  Contributor                                                   | Manager |
| ---------------------------------: | :------------------------------------------------------------------------------------------------------------: | :-----: |
|                 Can explore images |                                                      Yes                                                       |   Yes   |
|      Can add images to the project | Depend on [editing mode of the project](./project#editing-mode) and only for USER or ADMIN platform-wide roles |   Yes   |
|                Can annotate images |                        Depend on [editing mode of the project](./project#editing-mode)                         |   Yes   |
| Can add information to annotations |                        Depend on [editing mode of the project](./project#editing-mode)                         |   Yes   |
|             Can manage the project |                                                       No                                                       |   Yes   |

## Combinations of roles

Each user in Cytomine has a platform-wide role, and has a project-wide role in each project it is member of.

Any platform-wide role can be associated with any project-wide role.
| Roles | Guest | User | Admin |
| -: | :-: | :-: | :-: |
| **Contributor** | ✔ | ✔ | ✔ |
| **Manager** | ✔ | ✔ | ✔ |

The user will then benefit from the combination of the permissions of each role.

Example: a platform-wide GUEST can be MANAGER in a project: it will be able to manage the project using the configuration options, but will not be able to add images to the project as a GUEST do not have any image storage where to upload images.

## Examples of roles settings

Here are some examples of what combination of roles can be attributed to each user type regarding some common uses of Cytomine.
These are just examples, commonly used, and can differ from your needs.

### Teaching

|           Roles |  Guest  |              User               |                 Admin                 |
| --------------: | :-----: | :-----------------------------: | :-----------------------------------: |
| **Contributor** | Student |       Teaching assistant        |                   -                   |
|     **Manager** |    -    | Teaching assistant or Professor | Professor or University IT department |

### Research

|           Roles |    Guest    |                  User                   |                      Admin                       |
| --------------: | :---------: | :-------------------------------------: | :----------------------------------------------: |
| **Contributor** | Researchers |         Laboratory technicians          |                        -                         |
|     **Manager** |      -      | Lead researcher or Director of research | Director of research or Laboratory IT department |

### Diagnostic

|           Roles |              Guest               |                   User                    |                      Admin                      |
| --------------: | :------------------------------: | :---------------------------------------: | :---------------------------------------------: |
| **Contributor** | Collaborators or invited experts |          Laboratory technicians           |                        -                        |
|     **Manager** |                -                 | Lead pathologist or Director of pathology | Director of pathology or Hospital IT department |
