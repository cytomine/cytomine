ARG PROXY_CACHE=registry.cytomine.org/docker
ARG PYTHON_VERSION=3.10

FROM ${PROXY_CACHE}/python:${PYTHON_VERSION}-slim-bullseye AS base

COPY --from=ghcr.io/astral-sh/uv:0.8.13 /uv /uvx /bin/

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        build-essential libgl1 libglib2.0-0 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY pyproject.toml uv.lock /app/
COPY app configs /app/

RUN uv sync --locked


FROM ${PROXY_CACHE}/python:${PYTHON_VERSION}-slim AS production

COPY --from=ghcr.io/astral-sh/uv:0.8.13 /uv /uvx /bin/

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 libglib2.0-0 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY pyproject.toml uv.lock /app/
COPY app configs /app/

RUN uv sync --locked

CMD ["fastapi", "run", "app/main.py"]