# Cytomine-installer, or spawning Cytomine with docker-compose

A tool for deploying Cytomine with Docker Compose.

Scope and goals:
- the most simple and self-contained initial configuration for a cytomine instance: `cytomine.yml`, `docker compose.yml` and optional configuration files
- zero or minimal duplication of environment variables definition (e.g. urls, path, etc)
- a straightforward and simple way of converting the initial configuration files into deployment-ready files
- Cytomine services deployment with a simple `docker compose up` command (using the deployment files)

Out-of-scope:
- automatic generation of Docker Compose files
- Cytomine-specific hardcoded configuration (e.g. database, storage, etc)
- being a running service that interacts with a Cytomine installation to install, upgrade or configure it

Additional goals:
- deprecate `Cytomine-bootstrap` and `Cytomine-bootstrap-generator`

> **WARNING** Needs for auto-configuration and.or auto-upgrade are certain to arise in the future (if they have not already). The `installer` is not the place to implement this. It should be done as a separate service with another architecture. The `installer` is a one-shot tool that generates deployment files and nothing more. It is designed to be independent of Cytomine business logic and architecture which simplifies its implementation, maintenance and evolution.

# Docker images

> **Note** (@rmormont, 19/07/2024): since this was developed, the effort has been put on moving from runtime to build-time config file injection. This means that, altough supported by the installer, the injection of configuration files should be done at build time as much as possible and configuration should happen exclusively via environment variables.

The docker image encapsulating Cytomine services must all follow a set of conventions for interacting them with the installer logic.

Container configuration is done exclusively via environment variables and configuration files. These are bound to the container when it is spawned (with `docker compose up` for instance). Binding is defined with a `docker-compose.override.yml` file alongside the main `docker-compose.yml` file (no `docker cp` !!).

The environment files are attached via a volume mounted on `/cm_configs`. The full path of a configuration files can be summerized as `/cm_configs/{FILEPATH}/{CONFIG-FILE}[.sample]` where:

- `CONFIG-FILE` is the name of the configuration file
- `.sample` is an optional suffix indicating that file is **templated** and should be interpolated. It can be omitted if a file should not be interpolated at all.
- `FILEPATH` defines where the configuration files must eventually be located when the container starts.

It is the responsibility of the container to optionally interpolate the configuration files with values of corresponding environment variables and to place the files them within the right directories inside the container before launching the service. An example path is `/cm_configs/etc/nginx/sites-enabled/mysite.conf.sample` which means that the interpolated file `mysite.conf` must be placed in directory `/{FILEPATH}` where `FILEPATH=etc/nginx/sites-enabled`.

**In other words, the installer will only be responsible to mount the files in `/cm_configs` and nothing more (the rest is left to the container).**

## Template configuration files

See note in the [section above](#docker-images).

# Inputs

The inputs are the following:

- a `./cytomine.template` file: default environment variables configuration of an instance. It never contains instance-specific information and only ever changes from version control (never manually in an instance installation folder). It is tracked by version control.
- a `./cytomine.yml` file: actual default environment variables configuration of an instance. Generally, it supersedes the `cytomine.template` file by installer can be [configured](#install-configuration) to circumvent that. It is the only file that can be modified manually on the instance. It is not tracked by version control.
- a `docker-compose.yml` file defining what and how services should run. Never manually changed on an instance server. It is tracked by version control.
- an optional subfolder of configuration files to mount at deployment: `./configs/{SERVICE}/{FILEPATH}/{CONFIG-FILE}[.sample]`. The `SERVICE` denotes which service will receive the configuration files in the underlying directory tree. Within the container of this service, the file `{CONFIG-FILE}[.sample]` will be available in path `/cm_configs/{FILEPATH}`.

## Compose files `docker-compose.yml`

All images may use environment variables to be interpolated when `docker compose up` is called. These variables are defined in the `global` section of the `cytomine.yml` file (see next section).

## A environment configuration file: `cytomine.yml`

This files condenses the definition of environment variables necessary to deploy the cytomine instance in a declarative way.

The hierarchy in `cytomine.yml` is the following:

- `global` section:  `global.{namespace}.{value-type}.{env-var-name}.{value}`
- `services` section:  `services.default.{service}.{value-type}.{env-var-name}.{value}`

The top-level sections:

- `global`: for defining global variables that can be referred to later in the `cytomine.yml` `services` section (see below) and in the `docker-compose.yml` file(s). These variables will only be used by the installer and will not be seen by the container unless specified.
- `services`: defines which environment variables will be attached to the containers

The subsections:

- `service`: name of a service. This must match the name of a service defined in the `docker-compose.yml` file.
- `value-type`: how the installer should interpret the specified `value` field to assign the value to the environment variable. Three possible types:
    - `constant`: read `value` as it is parsed by the YAML parser (`value` should be a primitive type)
    - `auto`: the value is auto-generated by a method specified by `value` when the installer is executed
    - `global` (not supported in `envs.global` section): value extracted from a variable defined in the `global` subsection (`value` should be `{namespace}.{env-var-name}`)
- `env-var-name`: case-sensitive name of the environment variable
- `value`: the value to interpret based on `value-type`

The `global` variables can be referred to in the docker compose files following the following naming convention: `${NAMESPACE}_{ENV-VAR-NAME}` or `${{{NAMESPACE}_{ENV-VAR-NAME}}}` (eg. `$IMGS_CORE` or `${IMGS_CORE}` if namespace is `imgs` and var name is `CORE`). Dashes/hyphens in namespaces will be converted into underscores in environment variables names.

### Automatic env generation
This section documents the accepted values for the `value-type` `'auto'`:

- plain strings (e.g. `random-uuid`) in which case this string denotes a parameter-free generation method (or a method where all parameters have default values)
- key-value list where one of the keys must be `type` and contain an identifier of the generation method. Other key-value pairs contain the parameter names and values required by the generation method.

A parameter-free generation method can also be declared as key-value list in which case only the `type` entry must be specified. Supported generation types in the following subsections.

The key-value list can also accept additional parameters:

- `freeze`, boolean, default `true`: whether or not to move this parameter and its generated value to the `constant` section of the new `cytomine.yml` file generated by the installer. If set to `false`, the generated value will only be set in the `.env` files and will remain in the `auto` section with the same `value`.

#### Random UUID

Type: `random-uuid`, parameter-free

Generates a random UUID such as `801acff7-ccc5-48b3-bf9f-52f55e530f88`.

#### Random OpenSSL string

Type: `openssl`

Generates a random string using `openssl` in base 64.

Parameters:
- (optional) `length`, int, default `32`, number of bytes

#### Random string

Type: `secret`

Generates a random string of a given length from the default alphabet (see below).

Parameters:
- (optional) `length`, int, default `0`: the number of characters in the final string
- (optional) `whitelist`, str, default `''`: characters to include for generation
- (optional) `blacklist`, str, default `''`: characters of the default alphabet to exclude for generation

The `whitelist` and `blacklist` parameters are mutually exclusive.

Default alphabet: `abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!"#$%&'()*+,-./:;<=>?@[\]^``_{|}~`

# Install configuration

Precedences between `cytomine.yml` and `cytomine.template` can be configured with a file `./installer_config.yml`.

Full specifications can be found in https://gitlab.cytom.in/cm/rnd/cytomine/tools/cytomine-installer-ce/-/issues/2#note_25190.

# Running the installer

Before a docker-compose can be up'ed, the installer must read the `cytomine.yml` and generate deployment files.

This can be done by mounting the directory containing the inputs files (`cytomine.yml`, etc.) to an installer container as a `/install` folder and launching it:

```shell
# the target user must have the user permissions so it should not be created by docker
mkdir /tmp/install_out
docker run -v $(pwd):/install --user "$(id -u):$(id -g)" --rm --it INSTALLER_IMAGE deploy -s /install
```

This will generate install deployment-ready files in your current folder (on the host):

- `cytomine.yml`: a clone of the initial cytomine.yml file where auto-generated variables have been optionally moved to the constant sections. This file can be used for future configuration change.
- `.env`: defines the environment variables to be interpolated in the docker-compose.yml file
- `docker-compose.yml`: an unmodified clone of the initial `docker-compose.yml` file
- `docker-compose.override.yml`: an override Docker Compose file to attach environment variables (using files in `envs`) and mounting configuration files to each service
- `envs/{SERVICE}.env`: .env files containing environment variables defined for the services
- `configs/{SERVICE}/{FILEPATH}/{CONFIG-FILE}`: configuration files organized by service.
