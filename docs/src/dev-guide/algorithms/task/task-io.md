---
title: Task I/O
---

# {{ $frontmatter.title }}

::: warning
App Engine and its Task system are still in BETA. Therefore, all information presented in this documentation is subject to potentially breaking changes.
:::

Tasks receive their inputs and write their outputs as files and directories within the Task container, specifically in an input folder (e.g., `/inputs`) and an output folder (e.g., `/outputs`). This section provides unified specifications for these files and directories, guiding developers on how to read inputs and write outputs.

## Table of content

- [Generalities](#generalities)
- Type-specific information:
  - [`boolean`](#boolean)
  - [`integer`](#integer)
  - [`number`](#number)
  - [`string`](#string)
  - [`geometry`](#geometry)
  - [`image`](#image)
  - [`array`](#array)

## Generalities

At the start of the Task container, input parameters are accessible as files and directories within the `/inputs` folder, ready for the Task to read. This location can be changed by specifying a different path in [`configuration.input_folder`](/dev-guide/algorithms/task/descriptor-reference#configuration-input-folder) in the descriptor.

During execution, the Task must write all outputs as files or directories in the `/outputs` folder. Similar to inputs, this default output location can be modified by specifying an alternative path in [`configuration.output_folder`](/dev-guide/algorithms/task/descriptor-reference#configuration-output-folder) in the descriptor. Files or directories placed outside this designated folder will be disregarded by the App Engine and not recognized as outputs. Should an output parameter file or directory be absent, or if its structure deviates from the specified requirements, the Task will be considered failed.

The structure of the files or directories for each parameter must comply with the specifications detailed below, which vary based on the parameter's type. Inputs and outputs share the same structure, with the only difference being the location of the files and directories within the `/inputs` and `/outputs` folders, respectively.

For both inputs and outputs, the naming of each file or directory must align with its respective **parameter identifier** as defined in the descriptor file, without incorporating any extra characters or spaces.

In the examples below, `<EOF>` represents the end of the file.

## `boolean`

Boolean values are represented as a single file containing either `true` or `false`. The file should not contain any additional characters or spaces.

Examples:

<code-group>
<code-block title="Value `true`">

```text
true<EOF>
```

</code-block>
<code-block title="Value `false`">

```text
false<EOF>
```

</code-block>
</code-group>

## `integer`

Number values are represented as a single file containing a single integer value. The file should not contain any additional characters or spaces. For example:

```text
VALUE<EOF>
```

where `VALUE` is an integer number in base 2 (prefix `0b`), 8 (prefix `0`), 10 (no prefix) or 16 (prefix `0x`). More formally `VALUE` must match the following regex:

```bash
[-+]?0b[0-1_]+ # (base 2)
|[-+]?0[0-7_]+ # (base 8)
|[-+]?(0|[1-9][0-9_]*) # (base 10)
|[-+]?0x[0-9a-fA-F_]+ # (base 16)
```

Example values:

- `42`
- `-1`
- `0`
- `3735931646`
- `0b11011110101011011100101011111110` (binary)
- `0xDEADCAFE` (base 16)
- `033653345376` (base 8)

## `number`

Number values are represented as a single file containing a single floating point value. The file should not contain any additional characters or spaces. For example:

```text
VALUE<EOF>
```

where `VALUE` is a floating point number in decimal or scientific notation or one of the special values for infinity or not-a-number. More formally `VALUE` must match the following regex:

```regex
[-+]?([0-9][0-9]*)?\.[0-9.]*([eE][-+][0-9]+)?
|[-+]?(inf|Inf|INF)
|(nan|NaN|NAN)
```

Example values:

- `685230.15`
- `6.8523015e+5`
- `685.23015e+03`
- `-inf`
- `NaN`

## `string`

String values are represented as a single file containing a single or multi-line string UTF8 encoded value. For example:

```text
STRING_VALUE<EOF>
```

Examples:

<code-group>

<code-block title="Single word">

```text
word<EOF>
```

</code-block>

<code-block title="Single line">

```text
this is a single line string<EOF>
```

</code-block>

<code-block title="Multi-line">

```text
this is a
multi-line
string<EOF>
```

</code-block>

</code-group>

## `geometry`

A geometry is represented as a single JSON file containing one valid GeoJSON object. The supported GeoJSON subtypes are comprehensively listed below:

- [`Point`](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.2)
- [`MultiPoint`](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.3)
- [`LineString`](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.4)
- [`MultiLineString`](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.5)
- [`Polygon`](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.6)
- [`MultiPolygon`](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.7)
- [`Circle`](https://learn.microsoft.com/en-us/azure/azure-maps/extend-geojson#circle) (as a GeoJSON feature)
- [`Rectangle`](https://learn.microsoft.com/en-us/azure/azure-maps/extend-geojson#rectangle) (as a GeoJSON feature)
- GeoJson [`Feature`](https://datatracker.ietf.org/doc/html/rfc7946#section-3.2): the feature's geometry must only include one of the supported plain geometries (`Point`, `MultiPoint`, `LineString`, `MultiLineString`, `Polygon`, `MultiPolygon`). Features properties will not be interpreted nor validated by the App Engine (except for `Rectangle` and `Circle`) but will be conserved if present.

Because this type targets individual or plain geometries, we deliberately exclude the following GeoJSON subtypes:

- [`FeatureCollection`](https://datatracker.ietf.org/doc/html/rfc7946#section-3.3)
- [`GeometryCollection`](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.8)

The coordinate system used to encode positions is a simple 2D cartesian coordinate system. Because we are not dealing with a latitude-longitude system, we can ignore the [antimeridian cutting specification](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.9) in GeoJSON.

<!-- TODO: add a note about the precision of the coordinates: support floating point but no guarantee to conserve all decimal places when there are a lot of them -->

When the geometry is derived from an image parameter (see [`outputs.{PARAM}.dependencies.derived_from`](/dev-guide/algorithms/task/descriptor-reference##outputs-param-dependencies-derived-from)), the origin of the cartesian coordinate system must be located at the top left corner of the image.

Examples:

<code-group>

<code-block title="Point">
```json
{
  "type": "Point",
  "coordinates": [100.0, 0.0]
}
```
</code-block>

<code-block title="Polygon">
```json
{
  "type": "Polygon",
  "coordinates": [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]
}
```
</code-block>

<code-block title="Circle">
```json
{
  "type": "Feature",
  "geometry": {
    "type": "Point",
    "coordinates": [-122.126986, 47.639754]
  },
  "properties": {
    "subType": "Circle",
    "radius": 100
  }
}
```
</code-block>

<code-block title="Rectangle">
```json
{
  "type": "Feature",
  "geometry": {
    "type": "Polygon",
    "coordinates": [[[5,25],[14,25],[14,29],[5,29],[5,25]]]
  },
  "properties": {
    "subType": "Rectangle"
  }
}
```
</code-block>

</code-group>

**References:**

- [IETF spec for GeoJSON](https://datatracker.ietf.org/doc/html/rfc7946#page-11)
- [Microsoft GeoJSON extension](https://learn.microsoft.com/en-us/azure/azure-maps/extend-geojson)

## `image`

Image files are provided in the input folder with their parameter name (the original filename is replaced and the extension is
removed).

For directory-based image formats like **WSI DICOM**:

- Images must be zipped when provided through the provisioning API
- This approach works well for image parameters or small arrays of images provisioned from Cytomine

In storage, directory-based images can be represented as a directory containing multiple files (for referenced datasets)


## `array`

An array parameter is represented by a directory containing comprising several files:

- a YAML file named `array.yml`, which contains metadata about the array (_e.g._ its size).
- an individual files for each element in the array, named based on the element's index within the array. The encoding of each file matches that of its specific subtype (for instance, a text file for simple types or a directory for complex collections).

For instance, for an input with this specification:

```yaml
my_array:
  # ...
  type:
    id: array
    subtype: integer
```

and `my_array` is [4, 1, -1]. Then the parameter would be encoded as

```bash
my_array/
├── array.yml
├── 0  # txt file
├── 1  # txt file
└── 2  # txt file
```

where files have the following content:

<code-group>
<code-block title="array.yml">
```yaml
size: 3
```
</code-block>

<code-block title="0">

```text
4<EOF>
```

</code-block>

<code-block title="1">

```text
1<EOF>
```

</code-block>

<code-block title="2">

```text
-1<EOF>
```

</code-block>

</code-group>

If `allow_missing` is `true`, then missing collection items have their file absent and, therefore, index skipped.
