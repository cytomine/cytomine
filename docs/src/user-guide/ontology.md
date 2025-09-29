---
title: Ontology
---

# Ontology and Term

## Ontology

A key concept of the Cytomine platform concerns the **ontologies**. With the ontologies, you will be able to take full advantage of the semantic enrichment features.

An _ontology_ is a **structured** vocabulary of user-specified **terms** (or **labels**) used for the semantic annotation of regions of interest in images. Terms can be organized into sub-categories.

An ontology can be shared between several projects but a project is linked to a single ontology.

### Ontology page

The **Ontology page** is reachable through the _Ontology_ button in the navigation bar.

![Ontology button](/images/user-guide/ontology/ontology-button.png)

This page is composed of the _Ontology panel_ and the _Ontology editor_.

![Ontology page](/images/user-guide/ontology/ontology.png)

#### Ontology panel

The **Ontology panel** displays the list of ontologies you created and the ones for the projects you are member of.

![Ontology panel](/images/user-guide/ontology/ontology-panel.png)

#### Ontology editor

At the right, the **ontology editor** displays information about the selected ontology in the list.

![Ontology editor](/images/user-guide/ontology/ontology-editor.png)

You have edition rights if

- you created the ontology;
- or, you are the project manager for all projects using the ontology;
- or, you are member of a project using the ontology and all projects using the ontology are in collaborative mode.

In the other cases, the ontology editor is read-only.

#### Add a new term

Click on **Add a term**. Choose a name and a color for your new term. The name and color can be edited at any time.

![Create a new term](/images/user-guide/ontology/create-term.png)

#### Edit a term

Click on **edit icon** and edit the name and/or the color of the term.

To move a term to or from a sub-category, drag and drop the term in the ontology tree.

#### Delete a term

Deleting a term also remove all existing relations between the terms and annotations. Click on **trash icon** next to the term to delete and confirm.

#### Delete ontology

An ontology can be deleted only if no project is using it. Click on **Delete** and confirm.

## Term

A **term** is a semantic label. Terms can be organized in a hierarchical model defined by an **ontology**.
Using terms is the preferred method to associate the regions of interest delimited by the annotations with a semantic meaning.
If you use a project without an associated ontology, you will not be able to exploit the features linked to this semantic concept.

A specific color is defined for each term that will be used to differentiate annotations with different terms on the [Image Viewer](./image-viewer.md).

![An annotation with a red term](/images/user-guide/ontology/viewer-annotation-properties.png)

You will be able to display only the wanted term with the **#** menu at the right of the image viewer.

![Viewer term menu](/images/user-guide/ontology/right-sidebar-term.png)

Cytomine will also, by default, display annotation by terms in the annotation list.

![List the annotations of the project](/images/user-guide/ontology/left-sidebar-annotations.png)
