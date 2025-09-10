ARG PROXY_CACHE=registry.cytomine.org/docker
ARG PYTHON_VERSION=3.10

# Base Stage
FROM ${PROXY_CACHE}/python:${PYTHON_VERSION}-slim-bullseye AS base

COPY --from=ghcr.io/astral-sh/uv:0.8.16 /uv /uvx /bin/

WORKDIR /app

COPY pyproject.toml uv.lock /app/
COPY app configs /app/

RUN --mount=type=cache,target=/root/.cache/uv \
    --mount=type=bind,source=uv.lock,target=uv.lock \
    --mount=type=bind,source=pyproject.toml,target=pyproject.toml \
    uv sync

COPY ./cbir/ ./cbir/
COPY ./pyproject.toml ./uv.lock ./README.md ./
COPY ./weights/resnet ./weights/resnet


RUN --mount=type=cache,target=/root/.cache/uv \
    --mount=type=bind,source=uv.lock,target=uv.lock \
    --mount=type=bind,source=pyproject.toml,target=pyproject.toml \
    uv sync


# Development Stage
FROM base AS dev-server

RUN apt-get update \
    && apt-get -y install --no-install-recommends --no-install-suggests openssh-server rsync


# Production Stage
FROM ${PROXY_CACHE}/python:${PYTHON_VERSION}-slim AS production

ENV PATH="/app/.venv/bin:$PATH" \
    PYTHONPATH=/app \
    PYTHONUNBUFFERED=1 \
    UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy

COPY --from=registry.cytomine.org/github/ghcr.io/astral-sh/uv:0.8.16 /uv /uvx /bin/

WORKDIR /app

RUN --mount=type=cache,target=/root/.cache/uv \
    --mount=type=bind,source=uv.lock,target=uv.lock \
    --mount=type=bind,source=pyproject.toml,target=pyproject.toml \
    uv sync --frozen --no-install-project

COPY ./pyproject.toml ./uv.lock ./
COPY ./cbir/ ./cbir/
COPY ./weights/resnet ./weights/resnet

RUN --mount=type=cache,target=/root/.cache/uv \
    uv sync

ARG CBIR_VERSION
ARG CBIR_REVISION

LABEL org.opencontainers.image.authors="uliege@cytomine.org" \
      org.opencontainers.image.url="https://uliege.cytomine.org/" \
      org.opencontainers.image.documentation="https://doc.uliege.cytomine.org/" \
      org.opencontainers.image.source="https://github.com/cytomine/cytomine" \
      org.opencontainers.image.vendor="Cytomine ULiege" \
      org.opencontainers.image.version="${CBIR_VERSION}" \
      org.opencontainers.image.revision="${CBIR_REVISION}"

CMD ["uvicorn", "cbir.app:app", "--host", "0.0.0.0", "--port", "6000"]
