---
title: Task descriptor reference
---

# {{ $frontmatter.title }}

::: warning
App Engine and its Task system are still in BETA. Therefore, all information presented in this documentation is subject to potentially breaking changes.
:::

The Task descriptor is a YAML file that documents in a declarative way what the Task is and does. It serves as a contract between the developer and the App Engine, detailing:

- How the App Engine should run the Task
- What the Task expects as inputs
- What the App Engine should expect as outputs

Additionally, it provides general information about the Task, such as its name, author, and identification details.

The structure of the Task is dictated by a JSON schema available at [Descriptor Schema](https://github.com/cytomine/cytomine/blob/main/app-engine/src/main/resources/schemas/tasks/task.v0.json).

## Table of content

- [General information](#general-information)
  - [`$schema`](#schema)
  - [`name`](#name)
  - [`namespace`](#namespace)
  - [`version`](#version)
  - [`description`](#description)
  - [`authors`](#authors)
    - [`authors.[].first_name`](#authors-first-name)
    - [`authors.[].last_name`](#authors-last-name)
    - [`authors.[].organization`](#authors-organization)
    - [`authors.[].email`](#authors-email)
    - [`authors.[].is_contact`](#authors-is-contact)
  - [`external`](#external)
    - [`external.source_code`](#external-source-code)
    - [`external.doi`](#external-doi)
- [Technical configuration](#technical-configuration)
  - [`configuration.input_folder`](#configuration-input-folder)
  - [`configuration.output_folder`](#configuration-output-folder)
  - [`configuration.image`](#configuration-image)
    - [`configuration.image.file`](#configuration-image-file)
  - [`configuration.resources.ram`](#configuration-resources-ram)
  - [`configuration.resources.gpus`](#configuration-resources-gpus)
  - [`configuration.resources.cpus`](#configuration-resources-cpus)
  - [`configuration.resources.internet`](#configuration-resources-internet)
- [Inputs](#inputs) and [Outputs](#outputs)
  - [`inputs`](#inputs)
  - [`inputs.{PARAM}.description`](#inputs-param-description)
  - [`inputs.{PARAM}.display_name`](#inputs-param-display-name)
  - [`inputs.{PARAM}.default`](#inputs-param-default)
  - [`inputs.{PARAM}.optional`](#inputs-param-optional)
  - [`inputs.{PARAM}.type`](#inputs-param-type)
  - [`inputs.{PARAM}.dependencies.matching`](#inputs-param-dependencies-matching)
  - [`outputs`](#outputs)
  - [`outputs.{PARAM}.description`](#outputs-param-description)
  - [`outputs.{PARAM}.display_name`](#outputs-param-display-name)
  - [`outputs.{PARAM}.type`](#outputs-param-type)
  - [`outputs.{PARAM}.dependencies.derived_from`](#outputs-param-dependencies-derived-from)
  - [`outputs.{PARAM}.dependencies.matching`](#outputs-param-dependencies-matching)
- [Types](#types)
  - [Type `boolean`](#type-boolean)
    - [`{BOOLEAN}.type.id`](#boolean-type-id)
  - [Type `integer`](#type-integer)
    - [`{INTEGER}.type.id`](#integer-type-id)
    - [`{INTEGER}.type.lt`](#integer-type-lt)
    - [`{INTEGER}.type.leq`](#integer-type-leq)
    - [`{INTEGER}.type.gt`](#integer-type-gt)
    - [`{INTEGER}.type.geq`](#integer-type-geq)
  - [Type `number`](#type-number)
    - [`{NUMBER}.type.id`](#number-type-id)
    - [`{NUMBER}.type.lt`](#number-type-lt)
    - [`{NUMBER}.type.leq`](#number-type-leq)
    - [`{NUMBER}.type.gt`](#number-type-gt)
    - [`{NUMBER}.type.geq`](#number-type-geq)
    - [`{NUMBER}.type.infinity_allowed`](#number-type-infinity-allowed)
    - [`{NUMBER}.type.nan_allowed`](#number-type-nan-allowed)
  - [Type `string`](#type-string)
    - [`{STRING}.type.id`](#string-type-id)
    - [`{STRING}.type.min_length`](#string-type-min-length)
    - [`{STRING}.type.max_length`](#string-type-max-length)
  - [Type `enumeration`](#type-enumeration)
    - [`{ENUM}.type.id`](#enum-type-id)
    - [`{ENUM}.type.values`](#enum-type-values)
  - [Type `geometry`](#type-geometry)
    - [`{GEOMETRY}.type.id`](#geometry-type-id)
  - [Type `file`](#type-file)
    - [`{FILE}.type.id`](#type-file)
    - [`{FILE}.type.max_file_size`](#file-type-max-file-size)
  - [Type `image`](#type-image)
    - [`{IMG}.type.id`](#img-type-id)
    - [`{IMG}.type.max_file_size`](#img-type-max-file-size)
    - [`{IMG}.type.max_width`](#img-type-max-width)
    - [`{IMG}.type.max_height`](#img-type-max-height)
    - [`{IMG}.type.formats`](#img-type-formats)
  - [Type `array`](#type-array)
    - [`{ARRAY}.type.id`](#array-type-id)
    - [`{ARRAY}.type.subtype`](#array-type-subtype)
    - [`{ARRAY}.type.min_size`](#array-type-min-size)
    - [`{ARRAY}.type.max_size`](#array-type-max-size)
- [Parameter reference](#parameter-reference)
- [Parameter dependencies](#parameter-dependencies)
- [Memory unit](#memory-unit)

## General information

### `$schema`

**Description**: Base JSON schema reference. Should be one of the Cytomine Task reference schema.

|              |          |
| ------------ | -------- |
| **Type**     | `string` |
| **Required** | `true`   |
| **Format**   | `uri`    |

### `name`

**Description**: The name of the Task.

|                |                                        |
| -------------- | -------------------------------------- |
| **Type**       | `string`                               |
| **Required**   | `true`                                 |
| **Format**     | Must match regex `^[a-zA-Z0-9_\s\-]+$` |
| **Min length** | 3                                      |
| **Max length** | 30                                     |

### `namespace`

**Description**: The namespace of the Task, must follow the reverse domain name notation. Associated with the Task [version](#version), it must uniquely identify the Task.

|              |                                                       |
| ------------ | ----------------------------------------------------- |
| **Type**     | `string`                                              |
| **Required** | `true`                                                |
| **Format**   | Must match regex `^[a-zA-Z0-9_]*(\.[a-zA-Z0-9_-]+)+$` |

### `version`

**Description**: The version of the Task. Associated with the Task [namespace](#namespace), it must uniquely identify the Task.

|              |                                             |
| ------------ | ------------------------------------------- |
| **Type**     | `string`                                    |
| **Required** | `true`                                      |
| **Format**   | [Semantic versionning](https://semver.org/) |

### `description`

**Description**: A text description of the Task.

|                |          |
| -------------- | -------- |
| **Type**       | `string` |
| **Required**   | `false`  |
| **Default**    | `""`     |
| **Max length** | 2048     |

### `authors`

**Description**: Lists the authors of the Task.

|               |         |
| ------------- | ------- |
| **Type**      | `array` |
| **Required**  | `true`  |
| **Min items** | 1       |

Example:

```yaml
authors:
  - first_name: John
    last_name: Doe
    email: jd@email.com
  - first_name: Jane
    last_name: Doe
    email: jad@email.com
    organization: JaD Corp.
    is_contact: true
```

The underlying author objects must have at least one property other than `is_contact` defined.

#### `authors.[].first_name`

**Description**: This author's first name.

|              |          |
| ------------ | -------- |
| **Type**     | `string` |
| **Required** | `false`  |

#### `authors.[].last_name`

**Description**: This author's last name.

|              |          |
| ------------ | -------- |
| **Type**     | `string` |
| **Required** | `false`  |

#### `authors.[].organization`

**Description**: This author's organization name.

|              |          |
| ------------ | -------- |
| **Type**     | `string` |
| **Required** | `false`  |

#### `authors.[].email`

**Description**: This author's email address.

|              |          |
| ------------ | -------- |
| **Type**     | `string` |
| **Required** | `false`  |

#### `authors.[].is_contact`

**Description**: Whether this author is a contact person for the Task.

|              |           |
| ------------ | --------- |
| **Type**     | `boolean` |
| **Required** | `false`   |

### `external`

**Description**: Lists external references (source code, scientific articles,...).

|              |          |
| ------------ | -------- |
| **Type**     | `object` |
| **Required** | `false`  |

Example:

```yaml
external:
  source_code: https://github.com/cytomine/my-awesome-task
  dois:
    - https://doi.org/10.1093/bioinformatics/btw013
```

#### `external.source_code`

**Description**: URI pointing to the source code of the Task.

|              |          |
| ------------ | -------- |
| **Type**     | `string` |
| **Required** | `false`  |
| **Format**   | `uri`    |

#### `external.doi`

**Description**: List of DOIs relevant to the Task.

|              |                        |
| ------------ | ---------------------- |
| **Type**     | `array of string[uri]` |
| **Required** | `false`                |

<!-- ### Technical configuration -->

All configuration properties are under a `configuration` object.

## Technical configuration

### `configuration.input_folder`

**Description**: Full path where the input folder must be mounted inside the container

|              |             |
| ------------ | ----------- |
| **Type**     | `string`    |
| **Required** | `false`     |
| **Default**  | `"/inputs"` |
| **Format**   | `path`      |

### `configuration.output_folder`

**Description**:

|              |              |
| ------------ | ------------ |
| **Type**     | `string`     |
| **Required** | `false`      |
| **Default**  | `"/outputs"` |
| **Format**   | `path`       |

#### `configuration.image.file`

**Description**: The absolute path to the Docker image archive relative to the [Task archive bundle](/dev-guide/algorithms/task/#how-to-create-a-task) root.
_It should be in the root directory_

|              |                              |
| ------------ |------------------------------|
| **Type**     | `string`                     |
| **Required** | `true`                       |
| **Format**   | `/{namespace}-{version}.tar` |

Example:

```yaml
configuration:
  image:
    file: /some.task-1.0.0.tar
```

### `configuration.resources.ram`

**Description**: The minimum amount of RAM that the Task requires.

|              |                             |
| ------------ | --------------------------- |
| **Type**     | `integer`                   |
| **Required** | `false`                     |
| **Default**  | `"1GiB"`                    |
| **Format**   | [Memory unit](#memory-unit) |

### `configuration.resources.gpus`

**Description**: The number of GPUs that the Task requires.

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |
| **Default**  | `0`       |

### `configuration.resources.cpus`

**Description**: The number of CPU cores that the Task requires.

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |
| **Default**  | `1`       |

### `configuration.resources.internet`

**Description**: Whether the Task requires internet access, or not.

|              |           |
| ------------ | --------- |
| **Type**     | `boolean` |
| **Required** | `false`   |
| **Default**  | `false`   |

## Inputs and outputs

### `inputs`

**Description**: The set of input parameters of the Task.

|              |          |
| ------------ | -------- |
| **Type**     | `object` |
| **Required** | `true`   |

Each input parameter must be provided as a properties of `inputs` where the property name is the identifier of the parameter (`PARAM`) and must match the following regex: `^[a-zA-Z0-9_]+$`.

### `inputs.{PARAM}.description`

**Description**: A text description of the parameter.

|              |          |
| ------------ | -------- |
| **Type**     | `string` |
| **Required** | `true`   |

### `inputs.{PARAM}.display_name`

**Description**: A human-readable name for the parameter

|              |          |
| ------------ | -------- |
| **Type**     | `string` |
| **Required** | `false`  |

### `inputs.{PARAM}.default`

**Description**: Default value for the parameter. Only available for some types (see [Types](#types)).

|              |                                  |
| ------------ | -------------------------------- |
| **Type**     | depends on `inputs.{PARAM}.type` |
| **Required** | `false`                          |

### `inputs.{PARAM}.optional`

**Description**: Whether or not this parameter is optional.

|              |           |
| ------------ | --------- |
| **Type**     | `boolean` |
| **Required** | `false`   |
| **Default**  | `false`   |

### `inputs.{PARAM}.type`

**Description**: Type of the parameter.

|              |                 |
| ------------ | --------------- | ------- |
| **Type**     | `object         | string` |
| **Required** | `true`          |
| **Format**   | [Types](#types) |

### `inputs.{PARAM}.dependencies.matching`

**Description**: References to other parameters (of type `array`) that matches this parameter. See [`matching` dependency](#dependency-matching).

**Note**: Only supported for parameters with the `array`.

|              |                                             |
| ------------ | ------------------------------------------- |
| **Type**     | `array of string`                           |
| **Required** | `false`                                     |
| **Format**   | [Parameter reference](#parameter-reference) |

### `outputs`

**Description**: The set of output parameters of the Task.

|              |          |
| ------------ | -------- |
| **Type**     | `object` |
| **Required** | `true`   |

Each output parameter must be provided as a properties of `outputs` where the property name is the identifier of the parameter (`PARAM`) and must match the following regex: `^[a-zA-Z0-9_]+$`.

### `outputs.{PARAM}.description`

**Description**: A text description of the parameter.

|              |          |
| ------------ | -------- |
| **Type**     | `string` |
| **Required** | `true`   |

### `outputs.{PARAM}.display_name`

**Description**: A human-readable name for the parameter

|              |          |
| ------------ | -------- |
| **Type**     | `string` |
| **Required** | `false`  |

### `outputs.{PARAM}.type`

**Description**: Parameter type, see [Types](#types).

|              |                 |
| ------------ | --------------- | ------- |
| **Type**     | `object         | string` |
| **Required** | `true`          |
| **Format**   | [Types](#types) |

### `outputs.{PARAM}.dependencies.derived_from`

**Description**: Reference to a input parameter this output parameter is derived from. See [`derived_from` dependency](#dependency-derived-from).

|              |                                                          |
| ------------ | -------------------------------------------------------- |
| **Type**     | `string`                                                 |
| **Required** | `false`                                                  |
| **Format**   | [Parameter reference](#parameter-reference) (input only) |

### `outputs.{PARAM}.dependencies.matching`

**Description**: References to other parameters (of type `array`) that matches this parameter. See [`matching` dependency](#dependency-matching).

**Note**: Only supported for parameters with the `array`.

|              |                                             |
| ------------ | ------------------------------------------- |
| **Type**     | `array of string`                           |
| **Required** | `false`                                     |
| **Format**   | [Parameter reference](#parameter-reference) |

## Types

App Engine Task system supports a wide array of types for inputs and outputs. This section presents how these type are documented in the descriptor.

In general, types have a string **identifier** (e.g. `integer`). They may or may not support default values. Typically, default values are only supported by simple primitive types. They may or may not support short forms: a short form is a way to define the type with only its identifier rather than a whole YAML object. This is the case when all type properties of a parameter can be left to their defaults.

Example of short form vs. long form:

```yaml
inputs:
  short_form_param:
    # ...
    type: integer

  long_form_param:
    # ...
    type:
      id: integer
```

### Type `boolean`

**Description**: Type for a boolean parameter.

|                           |           |
| ------------------------- | --------- |
| **Identifier**            | `boolean` |
| **Supports default**      | `true`    |
| **YAML type for default** | `boolean` |
| **Short form**            | `true`    |

Examples:

<code-group>
<code-block title="Short form">
```yaml
inputs:
  param:
    # ...
    type: boolean
```
</code-block>

<code-block title="Long form">
```yaml
inputs:
  param:
    # ...
    type:
      id: boolean
```
</code-block>
</code-group>

#### type `{BOOLEAN}.type.id`

**Description**: Identifier of the type.

|              |                      |
| ------------ | -------------------- |
| **Type**     | `string`             |
| **Required** | `true`               |
| **Format**   | constant `"boolean"` |

### Type `integer`

**Description**: type for an integer number

|                           |           |
| ------------------------- | --------- |
| **Identifier**            | `integer` |
| **Supports default**      | `true`    |
| **YAML type for default** | `integer` |
| **Short form**            | `true`    |

Available constraints:

- [`lt`](#integer-type-lt): _less than_
- [`leq`](#integer-type-leq): _less or equal_
- [`gt`](#integer-type-gt): _greater than_
- [`geq`](#integer-type-geq): _greater or equal_

Examples:

<code-group>
<code-block title="Short form">
```yaml
inputs:
  param:
    # ...
    type: integer
```
</code-block>

<code-block title="Long form">
```yaml
inputs:
  param:
    # ...
    type:
      id: integer
      geq: 0
```
</code-block>
</code-group>

#### `{INTEGER}.type.id`

**Description**: Identifier of the type.

|              |                      |
| ------------ | -------------------- |
| **Type**     | `string`             |
| **Required** | `true`               |
| **Format**   | constant `"integer"` |

#### `{INTEGER}.type.lt`

**Description**: Constrains the integer parameter to an exclusive upper bound. This constraint is mutually exclusive with [`leq`](#integer-type-leq).

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

#### `{INTEGER}.type.leq`

**Description**: Constrains the integer parameter to an inclusive upper bound. This constraint is mutually exclusive with [`lt`](#integer-type-lt).

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

#### `{INTEGER}.type.gt`

**Description**: Constrains the integer parameter to an exclusive lower bound. This constraint is mutually exclusive with [`geq`](#integer-type-geq).

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

#### `{INTEGER}.type.geq`

**Description**: Constrains the integer parameter to an inclusive lower bound. This constraint is mutually exclusive with [`gt`](#integer-type-gt).

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

### Type `number`

**Description**: Type for a floating point number.

|                           |          |
| ------------------------- | -------- |
| **Identifier**            | `number` |
| **Supports default**      | `true`   |
| **YAML type for default** | `number` |
| **Short form**            | `true`   |

Available constraints:

- [`lt`](#number-type-lt): _less than_
- [`leq`](#number-type-leq): _less or equal_
- [`gt`](#number-type-gt): _greater than_
- [`geq`](#number-type-geq): _greater or equal_
- [`infinity_allowed`](#number-type-infinity-allowed)
- [`nan_allowed`](#number-type-nan-allowed)

Examples:

<code-group>
<code-block title="Short form">
```yaml
inputs:
  param:
    # ...
    type: number
```
</code-block>

<code-block title="Long form">
```yaml
inputs:
  param:
    # ...
    type:
      id: number
      nan_allowed: true
      gt: 0
```
</code-block>
</code-group>

#### `{NUMBER}.type.id`

**Description**: Identifier of the type.

|              |                     |
| ------------ | ------------------- |
| **Type**     | `string`            |
| **Required** | `true`              |
| **Format**   | constant `"number"` |

#### `{NUMBER}.type.lt`

**Description**: Constrains the number parameter to an exclusive upper bound. This constraint is mutually exclusive with [`leq`](#number-type-leq).

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

#### `{NUMBER}.type.leq`

**Description**: Constrains the number parameter to an inclusive upper bound. This constraint is mutually exclusive with [`lt`](#number-type-lt).

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

#### `{NUMBER}.type.gt`

**Description**: Constrains the number parameter to an exclusive lower bound. This constraint is mutually exclusive with [`geq`](#number-type-geq).

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

#### `{NUMBER}.type.geq`

**Description**: Constrains the number parameter to an inclusive lower bound. This constraint is mutually exclusive with [`gt`](#number-type-gt).

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

#### `{NUMBER}.type.infinity_allowed`

**Description**: Whether or not this parameter accepts `inf` (infinity) as a valid floating point number.

|              |           |
| ------------ | --------- |
| **Type**     | `boolean` |
| **Required** | `false`   |
| **Default**  | `false`   |

#### `{NUMBER}.type.nan_allowed`

**Description**: Whether or not this parameter accepts `nan` (not a number) as a valid floating point number.

|              |           |
| ------------ | --------- |
| **Type**     | `boolean` |
| **Required** | `false`   |
| **Default**  | `false`   |

### Type `string`

**Description**: Type for a sequence of characters.

|                           |          |
| ------------------------- | -------- |
| **Identifier**            | `string` |
| **Supports default**      | `true`   |
| **YAML type for default** | `string` |
| **Short form**            | `true`   |

Available constraints:

- [`min_length`](#string-type-min-length)
- [`max_length`](#string-type-max-length)

Examples:

<code-group>
<code-block title="Short form">
```yaml
inputs:
  param:
    # ...
    type: string
```
</code-block>

<code-block title="Long form">
```yaml
inputs:
  param:
    # ...
    type:
      id: string
      max_length: 10
```
</code-block>
</code-group>

#### `{STRING}.type.id`

**Description**: Identifier of the type.

|              |                     |
| ------------ | ------------------- |
| **Type**     | `string`            |
| **Required** | `true`              |
| **Format**   | constant `"string"` |

#### `{STRING}.type.min_length`

**Description**: Constrains the length of this string parameter to be at least a minimum number of characters (inclusive bound). If omitted, the string can be empty.

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |
| **Default**  | `0`       |

#### `{STRING}.type.max_length`

**Description**: Constrains the length of this string parameter to be at least a minimum number of characters (inclusive bound).

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

### Type `enumeration`

**Description**: Type for an enumeration of fixed values.

|                           |               |
| ------------------------- | ------------- |
| **Identifier**            | `enumeration` |
| **Supports default**      | `true`        |
| **YAML type for default** | `string`      |
| **Short form**            | `false`       |

**Note**: default value of the parameter, if any, must match one the of enumeration values

#### `{ENUM}.type.id`

**Description**: Identifier of the type.

|              |                          |
| ------------ | ------------------------ |
| **Type**     | `string`                 |
| **Required** | `true`                   |
| **Format**   | constant `"enumeration"` |

#### `{ENUM}.type.values`

**Description**: List of accepted values for this enumeration parameter.

|              |                   |
| ------------ | ----------------- |
| **Type**     | `array of string` |
| **Required** | `true`            |

Constraints for the enumeration values:

- **Min length**: 1
- **Max length**: 256
- **Format**: matching regex `^[^\\r\\n]+$`

### Type `geometry`

**Description**: Type for a geometry parameter.

|                      |            |
| -------------------- | ---------- |
| **Identifier**       | `geometry` |
| **Supports default** | `false`    |
| **Short form**       | `true`     |

No available constraint.

Examples:

<code-group>
<code-block title="Short form">
```yaml
inputs:
  param:
    # ...
    type: geometry
```
</code-block>

<code-block title="Long form">
```yaml
inputs:
  param:
    # ...
    type:
      id: geometry
```
</code-block>
</code-group>

### Type `file`

**Description**: Type for a file parameter.

|                      |         |
| -------------------- | ------- |
| **Identifier**       | `file`  |
| **Supports default** | `false` |
| **Short form**       | `true`  |

Available constraints:

- [`max_file_size`](#file-type-max-file-size)

Examples:

<code-group>
<code-block title="Short form">
```yaml
inputs:
  param:
    # ...
    type: file
```
</code-block>

<code-block title="Long form">
```yaml
inputs:
  param:
    # ...
    type:
      id: file
      max_file_size: 100 MiB
```
</code-block>
</code-group>

#### `{FILE}.type.id`

**Description**: Identifier of the type.

|              |                   |
| ------------ | ----------------- |
| **Type**     | `string`          |
| **Required** | `true`            |
| **Format**   | constant `"file"` |

#### `{FILE}.type.max_file_size`

**Description**: Constrains the file to a maximum size. If omitted, no file size constraint applied.

|              |                             |
| ------------ | --------------------------- |
| **Type**     | `string`                    |
| **Required** | `false`                     |
| **Format**   | [Memory unit](#memory-unit) |

### Type `image`

**Description**: Type for an image parameter.

|                      |         |
| -------------------- | ------- |
| **Identifier**       | `image` |
| **Supports default** | `false` |
| **Short form**       | `true`  |

Available constraints:

- [`max_file_size`](#img-type-max-file-size)
- [`max_width`](#img-type-max-width)
- [`max_height`](#img-type-max-height)
- [`formats`](#img-type-formats)

Examples:

<code-group>
<code-block title="Short form">
```yaml
inputs:
  param:
    # ...
    type: image
```
</code-block>

<code-block title="Long form">
```yaml
inputs:
  param:
    # ...
    type:
      id: image
      max_file_size: 100 MiB
      formats:
        - png
        - jpeg
```
</code-block>
</code-group>

#### `{IMG}.type.id`

**Description**: Identifier of the type.

|              |                    |
| ------------ | ------------------ |
| **Type**     | `string`           |
| **Required** | `true`             |
| **Format**   | constant `"image"` |

#### `{IMG}.type.max_file_size`

**Description**: Constrains the image file to a maximum size. If omitted, no file size constraint applied.

|              |                             |
| ------------ | --------------------------- |
| **Type**     | `string`                    |
| **Required** | `false`                     |
| **Format**   | [Memory unit](#memory-unit) |

#### `{IMG}.type.max_width`

**Description**: Constraints the image maximum accepted width in pixel. If omitted, no width constraint.

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

#### `{IMG}.type.max_height`

**Description**: Constraints the image maximum accepted height in pixel. If omitted, no height constraint.

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

#### `{IMG}.type.formats`

**Description**: Constraints the image format to one of the listed. If omitted, all plain image formats supported by the App Engine are accepted:

- `png`
- `jpeg`
- `tiff` (RGB, planar)

|              |                                    |
| ------------ | ---------------------------------- |
| **Type**     | `array of string`                  |
| **Required** | `false`                            |
| **Default**  | `['dicom', 'jpeg', 'png', 'tiff']` |

### Type `array`

**Description**: Type for an array parameter.

|                      |         |
| -------------------- | ------- |
| **Identifier**       | `array` |
| **Supports default** | `false` |
| **Short form**       | `false` |

Available constraints:

- [`min_size`](#array-type-min_size)
- [`max_size`](#array-type-max_size)

Examples:

<code-group>
<code-block title="Short form">
```yaml
inputs:
  param:
    #...
    type:
      id: array
      subtype: integer
```
</code-block>

<code-block title="Long form">
```yaml
inputs:
  param:
    #...
    type:
      id: array
      subtype:
        id: array
        subtype: number
```
</code-block>
</code-group>

#### `{ARRAY}.type.id`

**Description**: Identifier of the type.

|              |                    |
| ------------ | ------------------ |
| **Type**     | `string`           |
| **Required** | `true`             |
| **Format**   | constant `"array"` |

#### `{ARRAY}.type.subtype`

**Description**: Type of the underlying array items.

|              |                 |
| ------------ | --------------- | ------- |
| **Type**     | `object         | string` |
| **Required** | `true`          |
| **Format**   | [Types](#types) |

**Note**: the `subtype` field follows the same specifications as the parameter `type` field.

#### `{ARRAY}.type.min_size`

**Description**: Constrains the number of items in the array to a minimum number (inclusive bound). If omitted, minimum size is `0`.

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |
| **Default**  | `0`       |

#### `{ARRAY}.type.max_size`

**Description**: Constrains the number of items in the array to a maximum number (inclusive bound). If omitted, no maximum size constraint applied.

|              |           |
| ------------ | --------- |
| **Type**     | `integer` |
| **Required** | `false`   |

## Parameter reference

Some descriptor properties require to specify a reference to another input or output parameter. A reference is encoded using the following format:

```text
(inputs|outputs)/{PARAMETER_ID}
```

Examples:

- `inputs/my_param`: a reference to an input parameter `my_param`
- `outputs/my_param2`: a reference to an output parameter `my_param2`

## Parameter dependencies

Because the relations between parameters of a Task are hidden in the Task implementation, it is not possible to document these dependencies without explicitely declaring them in the descriptor. Therefore, the descriptor specification supports a set of pre-defined dependencies:

- `derived_from`
- `matching`

In general, interpretation of this dependency is left for the platform (e.g. Cytomine) which can decided or not to use it.

### Dependency: `derived_from`

The `derived_from` dependency can only be associated with an output parameter and indicates that this parameter is derived from a specified input parameter. The declared dependency should reference this input parameter using a [parameter reference](#parameter-reference) (see [specification in descriptor reference](#outputs-param-dependencies-derived-from)).

_Use case_: for Cytomine to properly display the annotations generated for an input image, it needs to know that the geometries produced by the Task are related to a certain input image

### Dependency: `matching`

The `matching` dependency can only exist between **`array` parameters**. It indicates that:

- array `A` and linked array `B` must have the same size (i.e. number of items) when the Task is executed
- item at index `i` in array `A` and item at index `i` in array `B` are matched together

The `matching` dependency is

- **bidirectional**: the dependency only needs to be defined for one of the matching parameters to be considered valid for all matched parameters
- **transitive**: parameters `A` and `B` and matching parameters `B` and `C` results in parameters `A` and `C` to be matched as well

This dependency is declared in the descriptor as documented in:

- [`inputs.{PARAM}.dependencies.matching`](#inputs-param-dependencies-matching)
- [`outputs.{PARAM}.dependencies.matching`](#outputs-param-dependencies-matching)

This dependency will help external systems to further interpret inputs/outputs that are matched together. Use cases:

1. **array of tuples encoded as matched arrays**: array of geometries, their probabilities as an array of floats and their classes as another output array of enumeration. Each item of each collection contains one information for one prediction of the system
2. **the input array mapped with corresponding outputs**: one input is an array of images and one output is an array of arrays of geometries where each array of geometries are the prediction for one image of the input array

## Memory unit

Some descriptor properties expect their value to represent an amount of memory of some kind (e.g. RAM availability, maximum file size, etc). In this section, we present how such information should be formatted. The memory information will be presented as:

```text
{number} {unit}
```

where the `unit` part is optional and the spacing between `number` and `unit` is also optional.

### Numeric value

The App Engine accepts both floating-point and integer numbers as the numeric value preceding the unit. After performing the unit conversion, the resulting value is rounded up to the nearest integer. The rounding occurs either at the bit or byte level, depending on the context.

Example (see units [below](#units)):

| Notation      | In bytes                         | In bytes (rounded) | In bits                       | In bits (rounded) |
| ------------- | -------------------------------- | ------------------ | ----------------------------- | ----------------- |
| `2.25455 Kib` | `2.25455  * 2^10 / 8 = 288.5824` | `289`              | `2.25455  * 2^10 = 2308.6592` | `2309`            |

### Units

The unit is composed of an optional (decimal or binary) prefix and a suffix.

Supported decimal prefixes:

| Symbol | Name   | Amount          |
| ------ | ------ | --------------- |
| none   | /      | 10<sup>0</sup>  |
| `k`    | kilo   | 10<sup>3</sup>  |
| `M`    | mega   | 10<sup>6</sup>  |
| `G`    | giga   | 10<sup>9</sup>  |
| `T`    | tera   | 10<sup>12</sup> |
| `P`    | peta   | 10<sup>15</sup> |
| `E`    | exa    | 10<sup>18</sup> |
| `Z`    | zetta  | 10<sup>21</sup> |
| `Y`    | yotta  | 10<sup>24</sup> |
| `R`    | ronna  | 10<sup>27</sup> |
| `Q`    | quetta | 10<sup>30</sup> |

Supported binary prefixes:

| Symbol | Name | Amount         |
| ------ | ---- | -------------- |
| `Ki`   | kibi | 2<sup>10</sup> |
| `Mi`   | mebi | 2<sup>20</sup> |
| `Gi`   | gibi | 2<sup>30</sup> |
| `Ti`   | tebi | 2<sup>40</sup> |
| `Pi`   | pebi | 2<sup>50</sup> |
| `Ei`   | exbi | 2<sup>60</sup> |
| `Zi`   | zebi | 2<sup>70</sup> |
| `Yi`   | yobi | 2<sup>80</sup> |

The supported unit **suffixes** indicate whether the value is in bytes or bits:

- `B`, `Byte`, `byte` → byte
- `b`, `bit` → bit

Examples:

| Notation | In bytes               | In bits                  |
| -------- | ---------------------- | ------------------------ |
| `25KiB`  | `25 * 2^10 = 25600`    | `25 * 2^13 = 204800`     |
| `25Kib`  | `25 * 2^10 / 8 = 3200` | `25 * 2^10 = 25600`      |
| `25KB`   | `25 * 10^3 = 25000`    | `25 * 8 * 10^3 = 200000` |
