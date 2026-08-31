ARG PROXY_CACHE=registry.cytomine.org/docker

FROM ${PROXY_CACHE}/python:3.12-alpine AS builder

RUN apk add --no-cache \
    gcc \
    geos-dev \
    musl-dev

WORKDIR /app
COPY . .
RUN pip install --no-cache-dir --prefix=/install .

FROM ${PROXY_CACHE}/python:3.12-alpine

RUN apk add --no-cache geos

COPY --from=builder /install /usr/local
COPY . /app

WORKDIR /app

ENTRYPOINT ["python", "examples/import_datasets.py"]
