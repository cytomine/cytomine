---
title: Getting started
---

# Getting started

## Get an account

There are three main ways to get a personal account:

- If you have Cytomine installed at your institution, ask an account from your local administrator.
- Ask for an account on our demo instance via email at <a :href="'mailto:'+$cytomine.email">{{$cytomine.email}}</a>.
- [Install Cytomine on your laptop or your server](/admin-guide/ce/installation.md) (for Linux users only).

When you have a username and a password, go to the login page and ... Welcome to Cytomine!

![The login page](/images/user-guide/getting-started/login.png)

## Your dashboard

Once logged in, you are redirected to your dashboard. It contains your recent activity, the last opened image and some statistics.
If it is the first time you log in to Cytomine, it is quite empty as you don't have any activity yet.
When we will have some projects and images, your dashboard will be like the following screenshot.

![The user dashboard](/images/user-guide/getting-started/user-dashboard.png)

At the top of the screen, the Cytomine navigation bar allows you to access different spaces in order to manage your projects, ontologies and your uploaded images.
These concepts will be further detailed in this guide.

![The Cytomine navigation bar](/images/user-guide/getting-started/tabs.png)

At the right, the quick search form allows you to search projects and images by their name.
If you need an exhaustive list for your search, click on the **+** icon to be redirected to the advanced search panel.

![The search bar](/images/user-guide/getting-started/search.png)

Next to the quick search bar, at the top right of the screen, the account drop-down menu (displayed with your name) lets you find information about your personal account and your activity on Cytomine.

![The account drop down menu](/images/user-guide/getting-started/account-menu.png)

## Your account page

In the account drop-down menu in the top navigation bar, click on _Account_

![Account button](/images/user-guide/getting-started/navbar.png)

and change your password.

![Password panel](/images/user-guide/getting-started/password.png)

You will see some connection keys. They will be explained in the [Developer Guide](/dev-guide/README.md).

## Upload an image

In this guide, we will upload one image of the [University of Namur](http://unamur.be/) available through the [Cytomine Open Access Collection](https://cytomine.com/collection) as an example.

Go to the _Storage_ space to see your personal storage.

![Your storage space](/images/user-guide/getting-started/storage.png)

This is the right place to upload your images before visualising them in Cytomine.

Add one or several images at once with the green _Add Files_ button.
Then, click on _Start upload_ to launch the import of your images to Cytomine.
Once the images are fully imported, they will appear in the _Storage_ panel.
These images are ready to be added to a project in order to visualize them, as we will see in the next steps of this guide.

## Create a project

A **project** is a working space where you can organize, visualize, annotate and analyze images. This working space can also be shared between multiple Cytomine users.

Go to _Project_ space to see the list of the projects you have access to. Each line corresponds to a specific project.

![Your project list](/images/user-guide/getting-started/tab-project.png)

To create a new project, click on the _New Project_ button. A pop-up is displayed.

![Your project creation pop-up](/images/user-guide/getting-started/add-project.png)

Give a name to your project, then choose the project ontology. You can either

1. not use any ontology for your new project,
2. associate this new project to an existing ontology,
3. create a new ontology with the same name as this new project.

In this Getting Started guide, we will use the third option.

Then, create the project by clicking on the _Save_ button.
You are automatically redirected to the configuration page of this newly created project.
At this stage, we will keep the default project configuration.

Return to the project list by clicking on _Projects_ in the Cytomine navigation bar.
There is a new line corresponding to your new project.
This line can be extended to display the details of the project.

![Expand the details of a project.](/images/user-guide/getting-started/project-detail.png)

You can enter into your new project by clicking on the project name or on the _Open_ button.

## Add images

To be able to interact with the images, we need to associate them with our current project.
Go to the images panel and click on the "Add image" button.
You will see a list with all your previously uploaded images.

![Image list with one image](/images/user-guide/getting-started/image-list-1.png)

Click on the "Add" Button for an image then close the pop-up to see the selected image displayed in the image list.

![Image list without images](/images/user-guide/getting-started/image-list-2.png)

As in the project list, you can expand a line to display some details about the image (resolution, size, vendor, etc).

![Image detail](/images/user-guide/getting-started/image-detail.png)

## Browse an image

You can browse an image from the list of the associated images in this project. Click on the Open button to browse the image.

![Browsing through the selected image](/images/user-guide/getting-started/viewer.png)

Use the **+/-** buttons or your mouse wheel to zoom in your image and the left click to drag for browsing in the image.
On the right panel, you will find a menu related to your exploration: you can see the layer of other users and properties associated to annotations, change the color balance of the image, etc.
But currently, there is no annotation on this image.
We will add them in the next point.

## Upload a task

navigate to applications tab and upload the task zip archive from your machine.

![upload_task](/images/user-guide/getting-started/upload_task.png)

## Run a task

Running a task involves 2 steps as follows:
 #### 1. select the task from the dropdown list.
 #### 2. provision the inputs of the task, the inputs will be listed by name, depending on the type the user sees :
 - Field for numeric or textual values
 - Toggle for boolean values
 - Selector for images and annotations

 after provisioning all inputs click on **Run Task** button to execute

![upload_task](/images/user-guide/getting-started/run_task.png)

## Add annotations

On the top navigation bar into the browsing view, you can select a mode for adding annotation.
To draw a circle annotation, click on "Circle" then put your mouse on a zone of the image, click at the wanted center position of the annotation and move your mouse to determine the radius.
Various geometric forms are possible and also a free drawing tool.

Once the annotation is created, you will see an infobox that will allow you to get the annotation size, add a description, terms, tags, properties and so on.

![Draw an annotation using the free draw tool](/images/user-guide/getting-started/viewer-annotation.png)

## Add terms to ontology

The main interest of annotations are the terms that we can associate with them to classify annotations and analyze images.
Go to the project dashboard and click on the Ontology link. On this space, you are able to add a term and the associated color to your ontology.

![Term add to the ontology](/images/user-guide/getting-started/ontology.png)

## Your Workspace

In the top navigation bar, the Workspace drop down menu will list all the currently opened projects and images during this session and allow you to return quickly in these projects or images.

![The workspace drop down menu](/images/user-guide/getting-started/workspace.png)

If you followed the previous instructions on this page, you have the project test with the previously opened image in this list.

Now, return to the previously opened image by clicking on the "Workspace" button and clicking on the image's name.

## Associate terms to your annotation

Click on the Select button in the annotation bar to select your previously drawn annotation and you can now associate terms to your annotation.
The annotation has now a color.

![Annotation term in the viewer](/images/user-guide/getting-started/viewer-annotation-term.png)

You can continue to draw annotations and associate the right terms to your annotations.

## Configure your project

Until now, we have used the default configuration of our project, but you can customize the access, the displayed panel and so on.
Go to the Configuration panel to be able to set the UI access for your contributors (users in your project) and managers (administrators of your project), the software associated with this project, the image filters, and the global permissions (Read Only, Restricted or Classic project).

![Project configuration](/images/user-guide/getting-started/project-configuration.png)

As an example, in the Custom UI panel, we will be able to disable some annotation tools: the Ellipse will be not used at all and the other geometric forms must not be used by my contributors (but the managers will still be able to use it).

## Add other users

To simply use the collaborative aspects of Cytomine, you need to add other users in your project.
Go to the Configuration tab and add a new contributor by clicking on the "Add members" button.
You are now able to see the layer of its new user in an image and see its annotations.

![Add a new contributors](/images/user-guide/getting-started/project-members.png)

If you want to promote a contributor as a manager, you can click on the human icon in the "Role" column to modify its permissions in the project.

## What to do next?

You will find more information about the exposed concepts in the other pages of this section.
If you are not familiar with Cytomine, let's begin with the [Project](./project.md) concept.
