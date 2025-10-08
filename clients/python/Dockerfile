ARG PROXY_CACHE=registry.cytomine.org/docker

FROM ${PROXY_CACHE}/python:3.12-alpine

RUN apk add geos

COPY . /app

WORKDIR /app

RUN pip install --no-cache-dir .

ENTRYPOINT ["python", "examples/import_datasets.py"]
