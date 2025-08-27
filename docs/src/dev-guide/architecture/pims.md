---
title: Python Image Management Server (PIMS)
---

# {{ $frontmatter.title }}

PIMS is the Image Management Server used by Cytomine to manage all images imported to Cytomine in various formats, including whole-slide images (WSI) acquired by specific hardware scanners.

In this section, you will learn how to contribute to the development of the new Cytomine Image Management Server component, which is developed in Python. You will explore how to extend or implement new features, or develop support for additional image formats.

## PIMS features

PIMS manages all that is related to images on the Cytomine platform, including very larges images (several giga-pixels). Notably it:

- imports and saves imported images, and convert them if necessary
- provides tiled versions of these images, to be able to navigate through them with the Cytomine viewer
- extracts metadata (including vendor-locked ones) and histograms, whatever the image format
- creates thumbnails efficiently for images, including for WSIs
- provides any region of interest at any zoom from an image, to be visualized or to be used by an AI algorithm
- draws Cytomine annotations on image crops.

PIMS is able to **manage images up to 5 dimensions: width (X), height (Y), depth (Z), time (T) and channels (C)** and save multiple representations of the same image, each of them being optimized for some operations, such as: visualisation, hyperspectral extraction (for images > 2D), or histogram (for color manipulation).

Once an image is imported, its PIMS representation is independent of the original image format such that there is a single unified API provided by PIMS to get image outputs such that tiles, thumbnails or arbitrary windows whatever was the original input format of the image.

PIMS has a **powerful cache mechanism**, with several levels of caching to accelerate image response generation and reduce parsing of image metadata. See [Advanced features > Caching](./pims#cache) for more details.

PIMS comes with a **task queue to run heavy tasks such as image importation**. These heavy tasks are run in a separate process, and it ensures that the server will never be flooded or blocked by batch image importations. The number of concurrent tasks is configurable. See [Advanced features > Task queue](./pims#task-queue) for more details.

PIMS is **extensible through plugins**. These plugins add support for new image formats. They rely on PIMS code base but are implemented in independent repositories, possibly with other licenses than PIMS code base (Apache 2.0).

![PIMS with plugins](/images/dev-guide/components/pims/pims-with-plugins.png)

It replaces the previous IMS with its IIP services.

## Run development server

::: warning
By default, when you run the development server, some cache levels are not enabled and heavy computations are still done in an asynchronous way but task queue is not more running in separate processes and there is no guarantee that all tasks will be able to fall back on this. In most cases, this is not a problem (except if your developments concern caching and task queue, of course). See [Advanced features](./pims#advanced-features) to enable them for development.
:::

### With Docker

```bash
docker build -f docker/backend.dockerfile -t pims .
docker run -p 5000:5000 pims
```

**The server is running at [http://127.0.0.1:5000](http://127.0.0.1:5000) and API documentation is available
at [http://127.0.0.1:5000/docs](http://127.0.0.1:5000/docs).**

::: tip
At this stage, it is hard to use Docker for development because hot-reload is not enabled and
PIMS plugin system is not yet easily manageable in a Docker container for development. However,
as PIMS requires a lot of low-level dependencies, developing using Docker would be a benefit.
:::

### Locally

First, dependencies must be installed

1. Dependencies in `docker/backend.dockerfile` must be installed first. For plugins, prerequisites
   have to be
   installed manually, especially for `before_vips` and `before_python`. See
   `install_prerequisites.sh` in respective plugins.
2. `pip install -r requirements.txt`

Then, you need to complete the configuration file. By default, the `pims-config.env` file is used but some required settings need to be filled in this file. Alternatively, provide another configuration file path.

To run the development server, run:

```bash
CONFIG_FILE="/path/to/my/config.env" python -m pims.main
```

**The server is running at [http://0.0.0.0:5000](http://0.0.0.0:5000) and API documentation is available
at [http://0.0.0.0:5000/docs](http://0.0.0.0:5000/docs).**

In order to test PIMS without Cytomine, you can import images locally using

```bash
CONFIG_FILE="/path/to/config.env" python pims/importer/import_local_images.py --path /my/folder
```

### Environment variables

- `CONFIG_FILE`: path to a `.env` configuration file. Default to `pims-config.env` (but some required configuration
  settings need to be filled)
- `LOG_CONFIG_FILE`: path to a `.yml` Python logging configuration file. Default to `logging.yml`
- `DEBUG`: When set, enable debug tools and use `logging-debug.yml` as default logging configuration if not other
  log config file is specified.

::: tip
Configuration settings can be also given as environment variables and override values from `CONFIG_FILE`
:::

## Image representation

Internally, PIMS treats all images as **tiled pyramids**.

The **pyramid** representation is a well-known internal organization file schema for zooming efficiently in large images. This representation is made by subsampling with a factor (usually 2) along X and Y directions. The procedure is then applied to the resulting image and the cycle is repeated multiple times. If represented graphically, the entire representation looks like a pyramid where the basis is the original image at full resolution and the successive subsampled images are the successive tiers of the pyramid.

![Image pyramid representation](/images/dev-guide/components/pims/pyramid.png)

<p align="center">
Image credit: <a href="https://iipimage.sourceforge.io/documentation/images/">IIP Image</a>
</p>

The **tiled** representation is an internal organization file schema to efficiently extract a region of interest in large images. A virtual grid is drawn on the image and every element of the grid is a tile (often 256 or 512 pixels wide). When an image is encoded along a tiled schema, each tile can be decoded independently. When only a part of an image need to be displayed, only tiles matching this region needs to be decoded. On the contrary, image formats that do not use tiled representation, the whole image needs to be decoded before displaying it.

![Tile request viewer](/images/dev-guide/components/pims/tile-request-viewer.png)

In tiled pyramids, all pyramid tiers are tiled. The number of tiers in the pyramid is dependent of the subsampling factor and the stop criterion on subsampling is usually implemented such as the last tier is included in a single tile, which is thus dependent of the dimensions of the input image. To talk about a given tier, PIMS defines the following terminology:

- **level**: it follows the building analogy. Level 0 has the best resolution (the most detailed image, the largest image, basis of the image pyramid). Maximum admissible tier level depends on image.
- **zoom**: it follows the zoom notion in photography. Zoom 0 has the worst resolution (the less detailed image, the smallest image, top of the image pyramid). Maximum admissible zoom level depends on image.

![Image pyramid technical representation](/images/dev-guide/components/pims/pyramid-zoom-level.png)

To ease integration with Cytomine, PIMS introduces the concept of **normalized pyramid** and **normalized tile**.

A **normalized pyramid** is a pyramid where each tier is **half the size** of the previous tier, **rounded up** (subsampling factor `f=2` and rounding function is `ceil`) and where every tier is tiled with normalized tiles. There are as many tiers in the pyramid such that the **highest tier is smaller or equal to a normalized tile**.

**Normalized tiles** are non-overlapping rectangular regions covering an image at given resolution (a tier in a pyramidal image). Normalized tiles have a **width and height of 256 pixels**, except right-most and bottom-most tiles which might be smaller.

## Image outputs

PIMS API provides endpoints to get various image outputs for a given imported image. These outputs are: tiles, normalized tiles, thumbnails and windows which are described hereafter.

An image output is the combination of one or several requested image channels (C). By default, all image channels are considered. However, an image output can only concern one time point (T) and one focal plane (Z).

Note: it is planned to support image output with several time points and focal planes that have been aggregated with a reduction function (such as max, mean or median functions). This has been drafted in early PIMS specifications and is implemented at API level (deactivated for now), but implementation is missing at operation pipeline level.

### Operation pipeline

Once extracted from the input image, the image output is **processed through a pipeline** presented in the next figure. Every requested channel is processed independently regarding intensity stretching and gamma processing and then colorized according a pre-encoded color in image metadata or with a color or colormap given in the request. The channels are then combined to obtain the output image itself. This output image is optionally resized (if PIMS was unable to extract pixels at requested target size) and then optionally filtered before to be encoded as an image file.

![Image output response](/images/dev-guide/components/pims/pims-image-output.png)

#### Intensity stretching

They are given by `min_intensities` and `max_intensities` parameters. Can be a single value (applied on all requested channels) or a list of values, one by channel.

Minimum intensity value is the pixel value in the original image used as minimum intensity (black) to create the response. As a consequence, original image intensities lower than this value will be black in the response.
Maximum intensity value is the pixel value in the original image used as maximum intensity (white) to create the response.
As a consequence, original image intensities greater than this value will be white in the response.

Maximum allowed value depends on image pixel type and is equal to `2 * pow(pixel type)`.

Minimum and maximum intensities are closely related to the concepts of brightness and contrast.
Brightness is the visual perception of reflected light while contrast is the separation of the lightest and darkest parts of an image.
A minimum intensity increase leads to:

- a brightness decrease, which refers to an image's decreased luminance.
- a contrast increase, which darken shadows and lighten highlights.

A maximum intensity increase leads to:

- a brightness increase, which refers to an image's increased luminance.
- a contrast decrease, which darken highlights and lighten shadows.

#### Gamma processing

Gamma performs a non-linear histogram adjustment. Pixel intensities in the original image are raised
to the power of the gamma value.

If `gamma < 1`, medium-intensity objects become fainter while bright objects do not.

If `gamma > 1`, faint objects become more intense while bright objects do not.

#### Colorization

Every channel can be colorized by a color (often pre-encoded in the image metadata) or by a colormap.
A colormap is a function that maps the colors of the original image (source) to the colors of the response
image. The usage of colormap produces a false-color representation of the original image and helps at
understanding the image.

Valid colormap names can be found with the endpoint `/colormaps`. The colormap can be reversed by prepending the
colormap name with `!`.

#### Resizing

Depending on requested target size, a resizing operation could be needed. It happens when PIMS is unable to find the region at the requested target size in the image file.

For example, suppose you want a thumbnail of 256px of a square image with a pyramid with the 2 highest tiers having a size of 300px x 300px and 150px x 150px. As a thumbnail is asked, PIMS should extract pixel in an intermediate virtual tier of 256px x 256px. As this virtual tier does not exist, the _most appropriate_ tier is used (here the 300px x 300px one). Once pixel transformation operations have been performed, resizing to requested target size is done.

#### Filtering

An image filter is used to change the appearance of an image and helps at understanding the
source image. Valid filter names can be found with the endpoint `/filters`.

Already implemented filters are: Otsu threshold, Iso data threshold, Yen threshold, Mean threshold, Min threshold and color deconvolution with various histological stains.

#### Drawing

When requesting a window or an annotation crop, provided geometries can be drawn on the output image. Every geometry can be drawn with its own fill color, stroke color and width.

#### Output encoding

PIMS supports these image formats for output response:

1. JPEG (`.jpg`) - lossy compression, but fastest
2. PNG (`.png`) - lossless compression, but slower than JPEG (required for transparency)
3. WEBP (`.webp`) - best compromise between JPEG and PNG, but not always supported by browsers (for Cytomine Web-UI)

#### Examples

Here are toy examples demonstrating operation pipeline capabilities. From left to right, top to bottom:

1. Original RGB image
2. Increased gamma, faint intensity objects become more intense
3. For every channel, the intensity range is 0-200, meaning that all intensities higher than 200 are displayed as white.
4. Red intensities have been stretched in 0-50 range, green in 50-150 range and blue in 100-255 range. Then, gamma has been slightly increased.
5. Intensities have been stretched independently for the 3 channels, then gamma has been increased. Color mapping has been applied: red intensities (stretched in 6-255) are colorized in yellow, green ones (stretched in 0-244) are colorized in magenta and blues ones (stretched in 23-255) have been inverted (`!`), that is, dark intensities are light and vice-versa and then colorized in green.
6. Intensities have been stretched and gamma modified. An Otsu thresholding filter is then applied on the result.

![PIMS image output example](/images/dev-guide/components/pims/pims-demo-image-output.png)

### Output types

**Every output types have their own endpoints in the PIMS API and are described in details in the interactive PIMS API documentation at [http://0.0.0.0:5000/docs](http://0.0.0.0:5000/docs).** This API is **not** directly available to the Cytomine end-user. Authenticated Cytomine users have to use the Cytomine API which is handled by Cytomine-Core component. This is then the Core which communicates with PIMS using the private PIMS API as illustrated on next figure.

![PIMS core window request flow](/images/dev-guide/components/pims/pims-core-window-request-flow.png)

#### Tiles

Tiles are non-overlapping rectangular regions covering an image at given resolution (a tier in a pyramidal image). Tile size is fixed except right-most or bottom-most tiles which might be smaller. **It depends on
internal image parameters.** Common tile sizes are `256*256` and `512*512`. Non tiled file formats only have
a single tile par tier.

- **Input region**: Tile index and tier (zoom or level) or Tile coordinates and tier (zoom or level)
- **Target size**: Always defined by underlying image tile size.

::: tip API

- Documentation: [http://0.0.0.0:5000/docs#tag/Tiles](http://0.0.0.0:5000/docs#tag/Tiles)
- Main endpoints:
  - `GET http://0.0.0.0:5000/image/{filepath}/tile/zoom/{zoom}/ti/{ti}`
  - `GET http://0.0.0.0:5000/image/{filepath}/tile/level/{level}/ti/{ti}`
  - `POST http://0.0.0.0:5000/image/{filepath}/tile`

:::

#### Normalized tiles

Normalized tiles are non-overlapping rectangular regions covering an image at given resolution (a tier in a pyramidal image). Normalized tiles have a **width and height of 256 pixels**, except right-most and bottom-most tiles which might be smaller. The pyramid of normalized tiles is also normalized. Each tier is **half the size** of the previous tier, **rounded up**.

Cytomine viewer uses normalized tiles.

- **Input region**: Tile index and tier (zoom or level) or Tile coordinates and tier (zoom or level)
- **Target size**:
  - Width: 256 pixels, except for right-most tiles that might have size lower than 256.
  - Height: 256 pixels, except for bottom-most tiles that might have size lower than 256.

::: tip API

- Documentation: [http://0.0.0.0:5000/docs#tag/Normalized-Tiles](http://0.0.0.0:5000/docs#tag/Normalized-Tiles)
- Main endpoints:
  - `GET http://0.0.0.0:5000/image/{filepath}/normalized-tile/zoom/{zoom}/ti/{ti}`
  - `GET http://0.0.0.0:5000/image/{filepath}/normalized-tile/level/{level}/ti/{ti}`
  - `POST http://0.0.0.0:5000/image/{filepath}/normalized-tile`

:::

#### Thumbnails & resized

Thumbnails and resized are reduced-size versions of the full underlying image content. While resized are general purpose,
thumbnails are optimized for human visualisation.

- **Input region**: Always full underlying image.
- **Target size**: one of
  - Length (largest side)
  - Width
  - Height
  - Zoom (for resized only)
  - Level (for resized only)

::: tip API

- Documentation:
  - [http://0.0.0.0:5000/docs#tag/Thumbnails](http://0.0.0.0:5000/docs#tag/Thumbnails)
  - [http://0.0.0.0:5000/docs#tag/Resized](http://0.0.0.0:5000/docs#tag/Resized)
- Main endpoints:
  - `GET http://0.0.0.0:5000/image/{filepath}/thumb`
  - `GET http://0.0.0.0:5000/image/{filepath}/resized`
  - `POST http://0.0.0.0:5000/image/{filepath}/thumb`
  - `POST http://0.0.0.0:5000/image/{filepath}/resized`

:::

#### Windows

Windows are rectangular portions extracted from the underlying image content (input region) and optionally
rescaled (target size).

- **Input region**: one of
  - Region (top, left, width, height) and reference tier (zoom or level)
  - Tile index and tier (zoom or level)
  - Tile coordinates and tier (zoom or level)
- **Target size**: one of
  - Length (largest side)
  - Zoom
  - Level

::: tip API

- Documentation: [http://0.0.0.0:5000/docs#tag/Windows](http://0.0.0.0:5000/docs#tag/Windows)
- Main endpoints:
  - `POST http://0.0.0.0:5000/image/{filepath}/window`

:::

#### Annotations

Annotations are geometries that can be represented on an image in various ways.

**Mask**: The mask is a generated image where geometries are filled by their respective given fill colors. The background is black.

**Crop**: The crop is similar to an image window but where the transparency of the background can be adjusted. By default, the background transparency is set to 100 which is also known as _alpha mask_. When the background transparency is set to 0, foreground and background cannot be distinguished.

**Drawing**: Get an annotation crop (with apparent background) where annotations are drawn according to their respective fill color, stroke width and stroke color.

- **Input region**: always rectangular envelope of all geometries multiplied by an optional context factor.
- **Target size**: one of
  - Length (largest side)
  - Width
  - Height
  - Zoom
  - Level

::: tip API

- Documentation: [http://0.0.0.0:5000/docs#tag/Annotations](http://0.0.0.0:5000/docs#tag/Annotations)
- Main endpoints:
  - `POST http://0.0.0.0:5000/image/{filepath}/annotation/crop`
  - `POST http://0.0.0.0:5000/image/{filepath}/annotation/mask`
  - `POST http://0.0.0.0:5000/image/{filepath}/annotation/drawing`

:::

## Importing images

Importing images into PIMS is the action of creating an _upload_ directory with one or several representations of the original imported image so that it is efficiently readable by PIMS to produce image outputs.

Contrary to previous IMS, PIMS is able to import images and create the associated _upload_ directory and its representations without Cytomine. PIMS can thus be used, developed and tested without any connection to a Cytomine server. To import images from the command line:

```bash
CONFIG_FILE="/path/to/config.env" python pims/importer/import_local_images.py --path /my/folder
```

where the mandatory argument `path` is either an image file or a folder of images files.

### Uploads & roles

When PIMS receives an uploaded file, a new directory is created for this upload in the `ROOT` path (configured in settings). This directory has a name of the form `upload-<uuid>` where `<uuid>` is a randomly generated unique identifier.

#### Upload folder

This directory will contain the original uploaded files next to additional files such converted images or different representations in different formats that PIMS is able to read efficiently. Given the location of the file in the `upload-<uuid>` folder, PIMS is able to associate a role to the files and to choose the appropriate role for a given request. Indeed, the same image data can be represented in different ways, in different files, each of them serving different purposes.

This received upload file is saved with its original name in the `upload-<uuid>` folder. It has the `UPLOAD` role. This is the file that is returned when an export is asked (e.g. to download the original file from Cytomine).

#### Processed folder

The importation procedures creates a `processed` directory in `upload-<uuid>`. This directory will contain all representations that are understood by PIMS to efficiently generate image outputs. All files in `processed` folder have been processed during importation and are supposed to be directly readable by PIMS.

##### `ORIGINAL` role

This role is associated to the file that is used the extract the original metadata from the image. Located in `processed` directory, the file is _always_ named `original.<id-format>` where `<id-format>` is the identifier of an image format in PIMS. It is always in uppercase, and it is important to note that it does not (necessarily) match common image extensions. Examples of format identifiers are: `JPEG`, `PNG`, `NDPI`, `IMAGEJTIFF`, `PYRTIFF`, ...

When the original uploaded file (the one with `UPLOAD` role) is a single file, this representation is simply a symbolic link to the `UPLOAD` role. For example `ROOT/upload-1/processed/original.PYRTIFF` is a symbolic link to `ROOT/upload-1/myfile.tif`.

When the original uploaded file is in a multi-file image format, it has been uploaded as an archive. In this case, the `UPLOAD` representation is the archive while the `ORIGINAL` is the extracted folder from the archive. For example, `ROOT/upload-2/processed/original.MRXS/` (folder) is the content of the archive located at `ROOT/upload-2/my-mrxs.zip`.

##### `SPATIAL` role

This representation is used to retrieve regular 2D spatial regions from the image. The image format of this representation should efficiently extract pixel data for visualisation (such as in Cytomine viewer). The file has the `SPATIAL` role. Located in `processed` directory, the file is _always_ named `visualisation.<id-format>` where `<id-format>` is the identifier of an image format in PIMS. It is always in uppercase, and it is important to note that it does not (necessarily) match common image extensions. Examples of format identifiers are: `JPEG`, `PNG`, `NDPI`, `IMAGEJTIFF`, `PYRTIFF`, ...

If the identified image format is directly readable by PIMS (e.g. pyramidal tiff), `ROOT/upload-1/processed/visualisation.PYRTIFF` is simply a symbolic link to `ROOT/upload-1/original.PYRTIFF` (which is itself a symbolic link to the `UPLOAD` role in this case).

If the identified image format is not directly readable by PIMS (e.g. Zeiss CZI), `ROOT/upload-3/processed/visualisation.PYRTIFF` is the result of the conversion in pyramidal TIFF of the original representation located at `ROOT/upload-3/processed/original.CZI`.

##### `SPECTRAL` role

This optional representation is used to retrieve spectral data from the image. The image format of this representation should efficiently extract pixel data for spectral visualisation, such as the intensities of a pixel across all image channels. This is the role used for spectral requests.

::: danger
Note: This has been drafted in early PIMS specifications but is implemented experimentally at API level. API specification should be reviewed. Also, conversions to `SPECTRAL` role after import procedure is not implemented.
:::

##### `HISTOGRAM` role

This representation is computed during import procedure. Located in the `processed` directory, the file is _always_ named `histogram`. It is used internally by the operation pipeline (e.g. to get minimum and maximum intensities or to apply thresholding filters).

#### Extracted folder

When the uploaded file is an archive but that does not match any format (it is thus not a multi-file format), PIMS considers the archive as a collection of independent images. The archive content is extracted recursively in `ROOT/upload-X/extracted/` folder. All files in this folder are then considered as individual imports. For example, if `extracted` folder has 2 files `img1.png` and `img2.jpg`, new upload directories are created:

- `ROOT/upload-Y/img1.png` (which is a symbolic link to `ROOT/upload-X/extracted/img1.png`) and import procedure is launched as if it was directly uploaded.
- `ROOT/upload-Z/img2.jpg` (which is a symbolic link to `ROOT/upload-X/extracted/img2.jpg`) and import procedure is launched as if it was directly uploaded.

### Import flow

The import flow generally follows these steps:

1. Extracting data: we start the import procedure, move the pending upload to its `upload` directory and ensure the file exists.
2. Detecting format: we try to find the PIMS image format that has to be used to read the imported file. The `match()` method of the format's checkers is used to that purpose.
3. Checking integrity: we ensure that PIMS is able to read image metadata and extract pixels from the file. It avoids to mark a file as correctly imported if a format is identified but the file is corrupted.

Sometimes, a conversion is required. See the different flows below.

The import flow emits **events** at the beginning and the end (successful or failure) of every step. Event listeners can be associated to implement actions related to these events. There are currently 2 implemented event listeners:

1. `StdoutListener`: print to the console information about the importation procedure (can be used for debugging)
2. `CytomineListner`: communicate events to Cytomine-Core and manages related `UploadedFile` and their status.

#### Import image without conversion

![PIMS import image without conversion](/images/dev-guide/components/pims/pims-import-image-without-conversion.png)

This flow happens when the original image format is a single-file format and PIMS is able to read pixels from the image efficiently. Example: pyramidal TIFF image.

| File                                         | Role      | Symbolic link ?       |
| -------------------------------------------- | --------- | --------------------- |
| ROOT/upload-X/img.tif                        | UPLOAD    | /                     |
| ROOT/upload-X/processed/original.PYRTIF      | ORIGINAL  | ROOT/upload-X/img.tif |
| ROOT/upload-X/processed/visualisation.PYRTIF | SPATIAL   | ROOT/upload-X/img.tif |
| ROOT/upload-X/processed/histogram            | HISTOGRAM | /                     |

#### Import image with conversion

![PIMS import image with conversion](/images/dev-guide/components/pims/pims-import-image-with-conversion.png)

This flow happens when the original image format is a single-file format but PIMS cannot read pixels efficiently. Example: large planar TIFF image.

| File                                         | Role      | Symbolic link ?       |
| -------------------------------------------- | --------- | --------------------- |
| ROOT/upload-X/img.tif                        | UPLOAD    | /                     |
| ROOT/upload-X/processed/original.PLANARTIF   | ORIGINAL  | ROOT/upload-X/img.tif |
| ROOT/upload-X/processed/visualisation.PYRTIF | SPATIAL   | /                     |
| ROOT/upload-X/processed/histogram            | HISTOGRAM | /                     |

#### Import multi-file image without conversion

![PIMS multi-file-image without conversion](/images/dev-guide/components/pims/pims-import-multi-file-image-without-conversion.png)

This flow happens when the original image format is a multi-file format (a directory with several files) and PIMS is able to read pixels from it efficiently. Examples: MRXS, VMS.

| File                                               | Role      | Symbolic link ?                        |
| -------------------------------------------------- | --------- | -------------------------------------- |
| ROOT/upload-X/img.zip                              | UPLOAD    | /                                      |
| ROOT/upload-X/processed/original.MRXS/ (directory) | ORIGINAL  | /                                      |
| ROOT/upload-X/processed/visualisation.MRXS         | SPATIAL   | ROOT/upload-X/processed/original.MRXS/ |
| ROOT/upload-X/processed/histogram                  | HISTOGRAM | /                                      |

#### Import multi-file image with conversion

![PIMS multi-file-image with conversion](/images/dev-guide/components/pims/pims-import-multi-file-image-with-conversion.png)

#### Import collection (archive)

![PIMS import collection](/images/dev-guide/components/pims/pims-import-collection.png)

### Image formats

Here is the list of image formats currently supported by PIMS. This list can be extended through plugins.

Note that extension is given for information only. **PIMS never detects image format from the file extension**.

| Format             | Extension (informative) | Brightfield support | CZT support                                       | Sample                                                                                 | Remark           |
| ------------------ | ----------------------- | ------------------- | ------------------------------------------------- | -------------------------------------------------------------------------------------- | ---------------- |
| JPEG               | .jpeg .jpg              | Native              | /                                                 | /                                                                                      | /                |
| PNG                | .png                    | Native              | /                                                 | /                                                                                      | /                |
| WEBP               | .webp                   | Native              | /                                                 | /                                                                                      | /                |
| BMP                | .bmp                    | Native              | /                                                 | /                                                                                      | /                |
| PGM                | .pgm                    | Native              | /                                                 | /                                                                                      | /                |
| DICOM              | .dicom                  | Native              | through plugin (3)                                | /                                                                                      | Multifile format |
| JPEG 2000          | .jp2                    | Native              | /                                                 | /                                                                                      | /                |
| Planar TIFF        | .tif .tiff              | Native              | /                                                 | /                                                                                      | /                |
| Pyramidal TIFF     | .tif .tiff              | Native              | /                                                 | [earthworm-2](https://cytomine.com/collection/earthworm-2/earthworm-transversal-plane) | /                |
| Planar OME-TIFF    | .ome.tiff .ome.tif      | Native              | <Badge text="Yes" type="tip" vertical="middle"/>  | /                                                                                      | /                |
| Pyramidal OME-TIFF | .ome.tiff .ome.tif      | Native              | <Badge text="Yes" type="tip" vertical="middle"/>  | /                                                                                      | /                |
| ImageJ TIFF        | .tif .tiff              | Native              | <Badge text="Yes" type="tip" vertical="middle"/>  | /                                                                                      | /                |
| Olympus SIS TIFF   | .sis .tif .tiff         | Native              | /                                                 | /                                                                                      | /                |
| 3D Histech Mirax   | .mrxs                   | through plugin (1)  | <Badge text="No" type="error" vertical="middle"/> | [CMU-1](https://cytomine.com/collection/cmu-1/cmu-1-mrxs)                              | Multifile format |
| Ventana BIF        | .bif                    | through plugin (1)  | /                                                 | [os-1](https://cytomine.com/collection/os-1/os-1-bif)                                  | /                |
| Hamamatsu NDPI     | .ndpi                   | through plugin (1)  | <Badge text="No" type="error" vertical="middle"/> | [earthworm-111](https://cytomine.com/collection/earthworm-111/earthworm-sagital-plane) | /                |
| Philips TIFF       | .tif .tiff              | through plugin (1)  | /                                                 | /                                                                                      | /                |
| Leica SCN          | .scn                    | through plugin (1)  | /                                                 | [Rubus-stem](https://cytomine.com/collection/rubus-stem-slide/rubus-stem)              | /                |
| Leica Aperio SVS   | .svs                    | through plugin (1)  | /                                                 | [CMU-1](https://cytomine.com/collection/cmu-1/cmu-1-small-region)                      | /                |
| Hamamatsu VMS      | .vms                    | through plugin (1)  | /                                                 | [CMU-3](https://cytomine.com/collection/cmu-3/cmu-3-vms)                               | Multifile format |
| Zeiss CZI          | .czi                    | through plugin (2)  | <Badge text="Yes" type="tip" vertical="middle"/>  | /                                                                                      | /                |
| Leica LIF          | .lif                    | through plugin (2)  | <Badge text="Yes" type="tip" vertical="middle"/>  | /                                                                                      | /                |
| Nikon ND2          | .nd2                    | through plugin (2)  | <Badge text="Yes" type="tip" vertical="middle"/>  | /                                                                                      | /                |

- (1) [Openslide plugin](https://github.com/cytomine/pims-plugin-format-openslide)
- (2) BioFormats plugin
- (3) WSI Dicom plugin

### Archive formats

PIMS is able to unpack archives from various formats:

- **ZIP** (.zip)
- **TAR** (.tar) (uncompressed tar file)
- **GZTAR** (.tar.gz)
- **BZTAR** (.tar.bz)
- **XZTAR** (.tar.xz)

::: danger
Archive format list cannot be extended through plugins.
:::

## Development

PIMS is implemented in Python and use Python type hints.

### Frameworks & libraries

PIMS relies on [FastAPI](https://fastapi.tiangolo.com/). FastAPI is a modern, fast (high-performance), web framework for building APIs with Python 3.7+ based on standard Python type hints.

PIMS API is described through **OpenAPI** standard thanks to FastAPI and **Pydantic** library is used for data validation and settings management.

Image manipulation heavily relies on **libvips**, **numpy**, **scikit-image** and **tifffile**.

Annotations are manipulated with **Shapely** and **rasterio**.

### Support new image formats

To add a new image format, there are two ways to do so. The first one is to implement the image format directly in the PIMS core and the other one is to use plugins.

#### In PIMS core

It is possible to implement new image formats by implementing the Python files directly in the PIMS core. One example can be found [here](https://github.com/cytomine/pims-plugin-format-example). One Python file corresponds to an image format.

For example, for a format named XYZ, a file `XYZ.py` is created. To implement this new format, several things are important to know.

A format is defined by three main classes, a **checker**, a **parser** and a **reader**. The first thing to do is to verify if the new format inherits from another one. In this case, those three classes can inherit from the parent classes (and be adapted if needed). It is thus not always necessary to define those classes. For example, for the JPEG format, only the checker and parser classes have been adapted. If the format does not inherit from any other one, it inherit from the [`AbstractFormat`](https://github.com/cytomine/pims/blob/master/pims/formats/utils/abstract.py) and you can adapt the format your own way. The three classes defining the format are implemented the following way:

- The **checker** class `XYZChecker` allows to verify if the format of an image corresponds to the format XYZ. Several ways to implement the checker class exist. A simple way to do so is to verify if the image file signature corresponds to the signature of the format (as shown in the [`ExampleFormat`](https://github.com/cytomine/pims-plugin-format-example/blob/master/pims_plugin_format_example/example.py)). If the format detection is based on something else (a particular metadata, etc.), the checker class can inherit from the [`AbstractChecker`](https://github.com/cytomine/pims/blob/master/pims/formats/utils/checker.py) class to define your own checker class. To find the most appropriate way to check the image format, see the technical documentation of the format itself.

- The **parser** class `XYZParser` allows to parse the data of the image so that PIMS can use it. Three methods must be implemented to parse the data:

  1. `parse_main_metadata`: parses the information needed by PIMS to manage the image (e.g. image size). See [`pims-plugin-format-example`](https://github.com/cytomine/pims-plugin-format-example) to find what is the information that has to be provided to PIMS.

  2. `parse_known_metadata`: parses the information standardized in Cytomine which PIMS does not use, such as the magnification, the physical size, etc.

  3. `parse_raw_metadata`: parses additional information that neither Cytomine or PIMS need to manage the image. This information is format-dependent.

  4. If the image is pyramidal, one can define the `parse_pyramid` method. This method creates a [`Pyramid`](https://github.com/cytomine/pims/blob/master/pims/formats/utils/structures/pyramid.py) object by iteratively defining the pyramid levels.

- The **reader** class `XYZReader` allows to read an image but also the thumbnail, etc. Three methods are needed to be implemented:

  1. `read_window(self, region, out_width, out_height, c=None, z=None, t=None)`: allows to read a window from the image defined by the `region`. `out_width` and `out_height` are the dimensions of the output image and are managed by PIMS, one does not need to specify those.

  2. `read_thumb(self, out_width, out_height, precomputed=None, c=None, z=None, t=None)`: allows to read the thumbnail image. If it already exists, `precomputed=True` and one just needs to return the thumbnail image. Otherwise, this method calls `read_window` for a region defined by the dimensions `out_width` and `out_height`. The region level is defined by the function `most_appropriate_tier()` that returns the appropriate tier (i.e. level of the pyramid) for the given output region dimensions.

  3. `read_tile(self, tile, c=None, z=None, t=None)`: allows to read a tile extracted from the image. It calls the method `read_window` where the region is the tile.

  4. If the image has a label and/or a macro image associated to it, one can define the methods `read_label(self, out_width, out_height)` and `read_macro(self, out_width, out_height)`. One does not need to care about the parameters `out_width` and `out_height`, those are managed by PIMS.

A class called `XYZFormat` also needs to be implemented. If the image format inherits from no other formats, this class inherits from the `AbstractFormat` class. In this class, there are four mandatory class attributes that are needed to be specified (checker, parser, reader and histogram reader classes). For the histogram reader class, if there is no particular way to get the histogram, it simply inherits from the [`DefaultHistogramReader`](https://github.com/cytomine/pims/blob/master/pims/formats/utils/histogram.py). Then, one can adapt the class methods and properties if the `XYZFormat` class needs to be adapted regarding its parent class methods.

#### Through plugins

To add a new image format, it is possible to do it by using plugins. An example of the implementation of a new format plugin can be found [here](https://github.com/cytomine/pims-plugin-format-example).

To create a new plugin, the simplest way is to adapt the format plugin [`pims-plugin-format-example`](https://github.com/cytomine/pims-plugin-format-example). The different steps are:

1. Create a directory `pims-plugin-format-{name}` in the directory `pims`, where `{name}` is the name of the format plugin.

2. Copy/paste the content of [`pims-plugin-format-example`](https://github.com/cytomine/pims-plugin-format-example). In this directory, one can also add other files that are needed during the installation of the plugin (e.g. a SDK that has to be installed before installing the format plugin).

3. Adapt the content of `pims-plugin-format-{name}`: in `setup.py`, adapt the `NAME` variable with the name of the new format plugin. In `install_prerequisites.sh`, specify all the package dependencies needed before installing the format plugin.

4. Rename `pims_plugin_format_{name}` and adapt the file `__version__.py`.

One plugin can add one or more new image formats. In the directory `pims_plugin_format_{name}`, each .py file implements a new image format. See [In PIMS core](#in-pims-core) to implement the `.py` files corresponding to new image formats.

## Advanced features

### Cache

PIMS heavily relies on caching features at many levels.

#### Low-level cache

This cache can store data at Python object level, it is thus always enabled. Any class can extend `SimpleDataCache` to
have caching features for an object.

There is also the `cached_property` decorator which allows an attribute to be computed on first access and then cached for subsequent accesses.

#### Medium-level cache

Images are presented in `Image` objects that acts as facade in front of all specific image format implementation details. This `Image` class stores many data about an image. To avoid to feed again and again data from image file at every request, these image information are stored in an LRU (least recently used) in-memory cache.

It is particularly useful with tiles as many tile requests for a same image are often asked in a short period of time. The `Image` object for this image thus need to be fed only once and can be reused for the subsequent tile requests.

This cache is always enabled and LRU cache size can be configured with `MEMORY_LRU_CACHE_CAPACITY` setting.

#### High-level cache

Sending multiple times the same request with the same parameters to PIMS will always produce the same output (response) provided that:

1. image files are never modified on disk
2. PIMS source code remains the same

The first consideration can be put aside as it should never happen. When a response has been computed for a given request, it is thus desirable to cache this response and serve it to subsequent identical requests, until the PIMS version changes. It is the responsibility of the high-level cache. Requests with the parameters (including body for POST requests) are hashed and the computed response are saved in an external Redis in-memory database.

This high-level cache is deeply coupled with HTTP caching features. A response from a cacheable request comes with the following headers:

1. **`Cache-Control`**: `private, must-revalidate, max-age=X` where:
   - `private` indicates that the response can be stored only in a private cache (e.g. local caches in browsers).
   - `must-revalidate` indicates that the response can be stored in caches and can be reused while _fresh_. If the response becomes _stale_, it must be validated with the origin server before reuse.
   - `max-age=X` indicates that the response remains _fresh_ until X seconds after the response is generated on the origin server.
2. **`ETag`**: `W/<response-hash>` where
   - `<response-hash>` is a string the uniquely represents the requested resource
3. **`X-PIMS-Cache`**: `HIT` or `MISS` to indicate if the request was already in the cache or not.

Stored HTTP responses have two states: _fresh_ and _stale_. The fresh state usually indicates that the response is still valid and can be reused, while the stale state means that the cached response has already expired. The criterion for determining when a response is fresh and when it is stale is age. In HTTP, age is the time elapsed since the response was generated and is thus related to the `max-age` directive.

When the HTTP client sends request for a _stale_ response in the cache (too old to be considered usable), the client will send the value of its `ETag` in an `If-None-Match` header field. PIMS compares the ETag from the `If-None-Match` with the `ETag` associated to the request in the high-level PIMS cache, and it both values match, the server sends back a `304 Not Modified` status which tells the client that its cached version of the response is still _fresh_.

::: warning Cache-control directives and reload
In HTTP **response**:

- `Cache-Control: no-cache`: ask browser to always check for content updates while reusing stored content. It does this by requiring caches to revalidate each request with the origin server. (It does not mean "don't cache" !!).
- `Cache-Control: no-store`: any caches of any kind (private or shared) should not store this response.
- `Cache-Control: max-age=N`: indicates that the response remains fresh until N seconds after the response is generated.

In HTTP **request**:

- `Cache-Control: no-cache`: asks caches to validate the response with the origin server before reuse. It allows clients to request the most up-to-date response even if the cache has a fresh response.
- `Cache-Control: no-store`: allows a client to request that caches refrain from storing the request and corresponding response — even if the origin server's response could be stored (browsers generally do not support this).
- `Cache-Control: max-age=N`: indicates that the client allows a stored response that is generated on the origin server within N seconds.

Browsers implement **normal reload** (Ctrl+R) by sending `Cache-Control: max-age=0` and `If-None-Match: <etag>` so that intermediately stored responses are not used and request are validated on the server with the ETag.

Browsers implement **hard reload** (Ctrl+Shift+R) by sending `Cache-Control: no-cache`.

:::

To run PIMS with this high-level cache, the cache must be configured in the settings. As the cache uses an
external Redis in-memory database, it is required to launch a Redis instance. Using default values in PIMS
settings, run:

```bash
docker run -d --name pims-cache -p 6379:6379 redis
```

If the PIMS high-level cache cannot be reached at PIMS startup, this cache is automatically disabled.

### Task queue

Heavy computation like image imports are run in a task queue to prevent server flooding or
blocking. The task queue uses Celery workers and RabbitMQ to communicate, but a fallback is
possible when unavailable.

#### Celery and RabbitMQ

PIMS is pre-configured to run with the Cytomine RabbitMQ configuration (see [Cytomine-bootstrap for PIMS](https://github.com/Cytomine-ULiege/Cytomine-bootstrap/tree/pims)).
RabbitMQ broker can be launched without Cytomine, by changing username and default password for
task queue in PIMS settings by `guest`/`guest`, and then run:

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 --hostname rabbitmq rabbitmq:3.9
```

Then, you have to run a Celery worker:

```bash
CONFIG_FILE="/path/to/my/config.env" celery -A pims.tasks.worker worker -l info -Q
pims-import-queue -c 1
```

where `-c` is the concurrency level; 1 is enough for development.
See below for environment variables.

#### Fallback task queue

If task queue is disabled in PIMS configuration of if RabbitMQ is unreachable, heavy
computations are still done in an asynchronous way but task queue is not mo running in separate
processes and there is no guarantee that all tasks will be able to fall back on this.
