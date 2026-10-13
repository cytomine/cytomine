---
title: Annotations
---

# Annotations

## Concepts

List of the main concepts related to a project:

- _Annotation_: A Region Of Interest inside an image. It can be a polygon, a multipolygon or a point.
- _Annotation Layer_: Each user has its own layer containing its annotations.
- _Term_: A word from the ontology. The color attribute is used to draw the annotation background.
- _Ontology_: A structured vocabulary of user-specified terms used for the semantic annotation of regions of interest in images.

## Ontology

Editing the ontology can be done through the _Ontologies_ menu item or in a project by clicking on the ontology name in the project configuration which opens the Ontology editor.
The editor allows to rename the ontology, add, edit, or delete terms from the ontology.
It has to be noted that Term names and colors can be changed afterwards but not the chosen ontology at the project creation.

![Ontology page](/images/user-guide/annotation/ontology.png)

Once the ontology is defined, one can start semantically annotating whole-slide images.
You can define the annotation without terms but your annotations will be simple Region Of Interest without a semantic meaning.

## Create an annotation

Open an image and select one of the annotation tools in the navigation bar at the top of the browsed image.

![Annotation tools](/images/user-guide/annotation/annotation-toolbar.png)

- **Select**: Allows you to select an annotation.
- **Association**: It opens a term selection box and allows you to select a default term to associate to the annotations that you will draw.
- **Point**: Create an annotation representing a particular point in the image. The area is equal to 0.
- **Line**: Create a straight line.
- **Freehand line**: Create a freehand line by clicking then dragging the mouse.
- **Rectangle**: Select a point in the image, click to fix one corner of the rectangle then move your mouse and click to set the opposite corner.
- **Circle**: The clicked point will be the center of the circle. Move your mouse to change the radius.
- **Polygon**: Each clicked point will be a vertex of the polygon.
- **Freehand polygon**: Allows you to create freehand drawing. Click somewhere on the image and drag the mouse to continuously draw the annotation.
- **Info**: Allows you to display or hide the information box of the selected annotation (enabled only if an annotation is selected).
- **Fill**: Allows you to fill holes inside an existing annotation. You must select the annotation to fill before clicking on the Fill button.
- **Modify**: Allows you to select a side of an annotation and drag it to modify the selected annotation.
- **Pen +**: Allows you to add a Free Hand drawn ROI to an existing annotation.
- **Pen -**: Allows you to delete a Free Hand drawn ROI to an existing annotation.
- **Move**: Drag the selected annotation to move it on the image.
- **Rotate**: Allows you to rotate an annotation around its centroid.
- **Delete**: Allows you to delete the selected annotation.

The two following buttons are not dependent of the selected annotation!

- **Undo/Redo**: Allow you to cancel (resp. restore) the last modification made on an annotation of the image.

## Terms, Properties and Descriptions

### Terms

Once you have drawn an annotation, you can add a semantic meaning to it by associating a term of the project ontology.
To change the terms associated to an annotation, select the annotation then, in the information box, add or remove the corresponding terms.

![Add an annotation term](/images/user-guide/annotation/add-annotation-term.png)

### Properties

Complementary to terms, you can add properties displayable on the image.\
In the information box, you will be able to see the list of the properties associate to the current annotation.\
By clicking on the _Add_ button, you will be able to add a key-value property to the annotation.\
Then, on the right panel, when you choose a key, all the properties with this key will display the corresponding value through the annotations.

![Display properties](/images/user-guide/annotation/viewer-annotation-properties.png)

### Descriptions

You can also add complete descriptions to an annotation via the information box of a selected annotation.
The Rich Text Editor allows you to format your description and add images, link to videos, etc.

![Description](/images/user-guide/annotation/description.png)

## Annotation Layers

Each user has its own layer containing its annotations. By default, you only see your own layer when you open an image.\
You can add other user layers to see their annotations by adding them to the current loaded layers.
To that end, in the Annotation Layers panel, select a contributor from the drop-down list and click on the _Add_ button.

![Add the layer of another user](/images/user-guide/annotation/add-annotation-layer.png)

You can now see the annotation of the Admin user on the image.
For each layer, you can

- View or hide it. It is the first checkbox.
- Enable or disable modification on it. It is the second checkbox. It is possible to modify other user layers if the manager of the project allows it.

## Annotations tab

The annotations tab is a panel dedicated to listing all the annotations in the project with filters on the contributors, the images and the terms.
The corresponding annotations are displayed in squared boxes and the information box associated to this annotation can be displayed by clicking on the **+** icon.
If you click on a box, you will be redirected to the image at the right zoom & location to see the selected annotation.

![List the annotations of the project](/images/user-guide/annotation/sidebar-annotations.png)

## Exporting annotations

From the annotations tab, the _Download results_ box lets you export the project annotations.
Next to the _PDF_, _CSV_ and _Excel_ download buttons, the _Export annotations_ button downloads every annotation of the project as a single ZIP archive named `<date>_<project-name>_annotations.zip`.

![Export annotations](/images/user-guide/annotation/export-annotations.png)

The archive is not a flat dump of the annotations: it is a self-describing dataset that follows the [BigPicture](https://bigpicture.eu/) metadata standard (version 3.0.0).
The geometries themselves are stored as [GeoJSON](https://geojson.org/) files, and a set of XML metadata files describe how those files relate to the project, its images, its ontology and the annotators.

### Structure of the archive

All the content lives under a single `dataset/` root folder:

```text
dataset/
├── METADATA/
│   ├── annotation.xml        # links each image to its GeoJSON file
│   ├── dataset.xml           # describes the project as a dataset
│   ├── annotator.xml         # describes the annotators (users)
│   ├── ontology.xml          # references the exported ontology file
│   ├── annotation_task.xml   # references the annotation task description
│   └── policy.xml            # data sharing policy placeholder
├── ONTOLOGIES/
│   └── {ONTOLOGY-NAME}.json  # the project ontology terms
├── ANNOTATION_TASKS/
│   └── task.txt              # human-readable description of the export
└── ANNOTATIONS/
    ├── IMAGE_{ALIAS}_{DATE}.geojson
    └── ...                   # one GeoJSON file per annotated image
```

The `METADATA/` folder uses opaque aliases (for example `IMAGE_aBcDeFgHiJ`) to cross-reference entities between files instead of exposing internal Cytomine identifiers.
Every file referenced from the metadata carries a `SHA256` checksum so the integrity of the archive can be verified.

#### `ANNOTATIONS/*.geojson`

There is one GeoJSON file per annotated image, grouping all the annotations of that image into a single `FeatureCollection`.
Coordinates are expressed in image pixels. Each feature carries the associated ontology term, when any, in its `path_class_name` property:

```json
{
  "type": "FeatureCollection",
  "name": "IMAGE_aBcDeFgHiJ_20260101",
  "features": [
    {
      "type": "Feature",
      "properties": {
        "unit": "pixel",
        "path_class_name": "Tumor"
      },
      "geometry": {
        "type": "Polygon",
        "coordinates": [[[10, 10], [10, 50], [50, 50], [50, 10], [10, 10]]]
      }
    }
  ]
}
```

#### `METADATA/annotation.xml`

This file is the entry point of the dataset. It lists one `ANNOTATION` per annotated image and ties together the image, the GeoJSON file (with its checksum), the annotator, the ontology and the annotation task:

```xml
<?xml version="1.0" encoding="utf-8"?>
<ANNOTATION_SET>
  <ANNOTATION alias="ANNOTATION_aBcDeFgHiJ">
    <IMAGE_REF alias="IMAGE_kLmNoPqRsT"></IMAGE_REF>
    <FILES>
      <FILE filename="ANNOTATIONS/IMAGE_kLmNoPqRsT_20260101.geojson" filetype="geojson" checksum_method="SHA256" checksum="..." unencrypted_checksum="..."></FILE>
    </FILES>
    <ANNOTATOR_REF alias="ANNOTATOR_uVwXyZaBcD"></ANNOTATOR_REF>
    <ONTOLOGY_REF alias="ONTOLOGY_eFgHiJkLmN"></ONTOLOGY_REF>
    <TASK_REF alias="ANNOTATION_TASK_oPqRsTuVwX"></TASK_REF>
    <ATTRIBUTES>
      <STRING_ATTRIBUTE>
        <TAG>date_of_creation</TAG>
        <VALUE>2026-01-01T09:30:00</VALUE>
      </STRING_ATTRIBUTE>
    </ATTRIBUTES>
  </ANNOTATION>
</ANNOTATION_SET>
```

The other metadata files complete the description of the dataset:

- `dataset.xml` describes the project itself (title, description, version) and references every `ANNOTATION`.
- `annotator.xml` describes each annotator (the Cytomine users who created the annotations).
- `ontology.xml` references the ontology file stored under `ONTOLOGIES/` and its checksum.
- `annotation_task.xml` references the `ANNOTATION_TASKS/task.txt` description file.
- `policy.xml` is a placeholder for the data sharing policy of the dataset.

Because the export follows the BigPicture metadata standard, the archive can be imported again into Cytomine.
See [Datasets Importation](/admin-guide/docker/configuration.html#datasets-importation) in the administrator guide for the procedure.
