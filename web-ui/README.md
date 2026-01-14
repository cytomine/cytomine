# Cytomine Web UI

[![Apache-2.0 license][license-badge]][license-url]
[![Website][website-badge]][website-url]

## Presentation

The Cytomine Web UI is the official front-end for Cytomine, developed in [Vue.js](https://vuejs.org/).
You will find more information about the Cytomine project on the [website][website-url].
You can also find, on our official documentation [how to install it](https://doc.uliege.cytomine.org/admin-guide/ce/installation) and a [user guide](https://doc.uliege.cytomine.org/user-guide/).

The main features of this front-end are:

* Support of internationalization, i.e., multiple languages (English, French, Spanish, and Dutch currently available).
* Reactive application.
* Lists of project, image with sorting, searching and filtering.
* Display details of images and projects in all the lists.
* A redesigned image viewer focused on the image itself, with foldable secondary components The viewer is now compatible with tablets.
* Rotation of images at any specific angle.
* Set up of magnification and resolution of each image directly from the viewer or the image list.
* Zoom beyond the maximum resolution of an image by activating a digital zoom.
* Display more than one image into a viewer for efficient comparison. These opened images can be linked to synchronize your navigation and allow you to easily browse though multiple images at once.
* Draw annotations with a preselected term.
* Opacity of each term color can be specifically set up.
* Straight lines and Free hand lines annotations are now available.
* Brightness, Contrast, Saturation and Hue of an image can be set up during visualization.
* The Workspace feature allows you to open multiple projects and image and go back to a currently opened image or project.
* A quick search across the platform is available in the top navigation bar.
* An advanced panel allowing you to search all across the platform.
* Set for projects default annotation layers and default property which will be displayed at the opening of an image.
* Follow in real time another user viewer or part of viewer, only if this user have allowed the Cytomine platform to broadcast it.
* Simplification of user management in projects.
* An ontology can be changed in a project.
* Edition of ontology terms had been simplified.
* Attached files can be added to a project, an image, or an annotation.
* Manage files in storage is enriched by total size on disk and list of sub images for archived or converted images.

## Remarks

* If the front-end is not expected to run on the same host/port as the core, NGINX should be configured to allow CORS addressed to the core
* When using our software, we kindly ask you to show our website URL and our logo in all your work (website, publications, studies, oral presentations, manuals, ...). If you use Cytomine for scientific purpose, please cite Marée et al., Bioinformatics 2016 as reference paper. See our license files for additional details.
  - URL: <https://uliege.cytomine.org/>
  - Logo: [Available here](https://doc.uliege.cytomine.org/images/cytomine-uliege-logo.png)
  - Scientific paper: Raphaël Marée, Loïc Rollus, Benjamin Stévens, Renaud Hoyoux, Gilles Louppe, Rémy Vandaele, Jean-Michel Begon, Philipp Kainz, Pierre Geurts and Louis Wehenkel. Collaborative analysis of multi-gigapixel imaging data using Cytomine, Bioinformatics, DOI: 10.1093/bioinformatics/btw013, 2016. http://dx.doi.org/10.1093/bioinformatics/btw013

## License

[Apache 2.0][license-url].

[cytomine-url]: https://uliege.cytomine.org/
[license-badge]: https://img.shields.io/badge/License-Apache%202.0-blue.svg
[license-url]: https://github.com/cytomine/cytomine/blob/main/LICENSE
[website-badge]: https://img.shields.io/badge/Website-blue
[website-url]: https://uliege.cytomine.org/
