# All args are listed here at the top for readability
ARG OPENJPEG_URL=https://github.com/uclouvain/openjpeg/archive
ARG OPENJPEG_VERSION=2.5.2
ARG PIMS_REVISION
ARG PIMS_VERSION
ARG PLUGIN_CSV=scripts/plugin-list.csv
ARG PROXY_CACHE=registry.cytomine.org/docker
ARG PY_VERSION=3.10
ARG VIPS_URL=https://github.com/libvips/libvips/releases/download
ARG VIPS_VERSION=8.15.2

#######################################################################################
## Stage: Pims
FROM ${PROXY_CACHE}/python:${PY_VERSION}-bookworm AS base-pims

ARG OPENJPEG_URL
ARG OPENJPEG_VERSION
ARG PIMS_REVISION
ARG PIMS_VERSION
ARG PLUGIN_CSV
ARG VIPS_URL
ARG VIPS_VERSION

ENV DEBIAN_FRONTEND=noninteractive \
    LANG=C.UTF-8 \
    MODULE_NAME="pims.application" \
    PLUGIN_INSTALL_PATH=/app/plugins \
    PORT=5000 \
    PYTHONPATH="/app:$PYTHONPATH" \
    UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy \
    UV_PROJECT_ENVIRONMENT=/usr/local

RUN apt-get update && \
    apt-get -y install --no-install-recommends --no-install-suggests \
        libexif-dev \
        libgsf-1-dev \
        libimage-exiftool-perl \
        liblcms2-dev \
        libopenjp2-7-dev \
        libopenslide-dev \
        liborc-dev \
        libpng-dev \
        libtiff-dev \
        libvips-dev \
        libvips42 \
        libwebp-dev

COPY --from=registry.cytomine.org/github/ghcr.io/astral-sh/uv:0.8.13 /uv /uvx /bin/

# Download plugins
WORKDIR /app
COPY ./pims/docker/plugins.py /app/plugins.py
COPY ./pims/${PLUGIN_CSV} /app/plugins.csv
COPY ./pims/plugins/ /app/plugins/

RUN python plugins.py \
        --plugin_csv /app/plugins.csv \
        --checkerResolution_file_path /app/checkerResolution.csv \
        --method checker_resolution_file

# Run before_python() from plugins prerequisites
RUN python plugins.py \
        --plugin_csv /app/plugins.csv \
        --install_path ${PLUGIN_INSTALL_PATH} \
        --method dependencies_before_python

# Cleaning. Cannot be done before as plugin prerequisites could use apt-get.
RUN rm -rf /var/lib/apt/lists/*

COPY ./clienxdts/python /clients/python

RUN --mount=type=cache,target=/root/.cache/uv \
    --mount=type=bind,source=./pims/uv.lock,target=uv.lock \
    --mount=type=bind,source=./pims/pyproject.toml,target=pyproject.toml \
    uv sync --frozen --no-install-project

# Add default config
COPY ./pims/pims-config.env /app/pims-config.env
COPY ./pims/logging-prod.yml /app/logging.yml
COPY ./pims/docker/gunicorn_conf.py /app/gunicorn_conf.py

COPY ./pims/docker/start.sh /start.sh
RUN chmod +x /start.sh

COPY ./pims/docker/start-reload.sh /start-reload.sh
RUN chmod +x /start-reload.sh

COPY ./pims/pims /app/pims
COPY ./pims/resources/ ./resources/

COPY ./pims/pyproject.toml /pims/uv.lock ./
RUN --mount=type=cache,target=/root/.cache/uv \
    uv sync

RUN python plugins.py \
    --plugin_csv /app/plugins.csv \
    --install_path ${PLUGIN_INSTALL_PATH} \
    --method install && \
    rm -f /app/plugins.csv

LABEL org.opencontainers.image.authors='uliege@cytomine.org' \
      org.opencontainers.image.url='https://uliege.cytomine.org/' \
      org.opencontainers.image.documentation='https://doc.uliege.cytomine.org/' \
      org.opencontainers.image.source='https://github.com/cytomine/cytomine/tree/main/pims' \
      org.opencontainers.image.vendor='Cytomine ULiege' \
      org.opencontainers.image.deps.openjpeg.url=${OPENJPEG_URL} \
      org.opencontainers.image.deps.openjpeg.version=${OPENJPEG_VERSION} \
      org.opencontainers.image.deps.vips.url=${VIPS_URL} \
      org.opencontainers.image.deps.vips.version=${VIPS_VERSION} \
      org.opencontainers.image.plugin.csv=${PLUGIN_CSV} \
      org.opencontainers.image.revision=${PIMS_REVISION} \
      org.opencontainers.image.version=${PIMS_VERSION}

EXPOSE ${PORT}

#######################################################################################
## Stage: Pims (dev server)
FROM base-pims AS dev-server

RUN apt-get -y update && \
    apt-get -y install --no-install-recommends --no-install-suggests openssh-server rsync

#######################################################################################
## Stage: production
FROM base-pims AS production

CMD ["/start.sh"]
