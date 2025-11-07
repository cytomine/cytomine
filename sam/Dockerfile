ARG PROXY_CACHE=registry.cytomine.org/docker
ARG PYTHON_VERSION=3.10

FROM ${PROXY_CACHE}/python:${PYTHON_VERSION}-slim-bullseye AS base

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy \
    UV_PROJECT_ENVIRONMENT=/usr/local

COPY --from=ghcr.io/astral-sh/uv:0.8.13 /uv /uvx /bin/

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        build-essential libgl1 libglib2.0-0 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY pyproject.toml uv.lock /app/
COPY app configs /app/

RUN --mount=type=cache,target=/root/.cache/uv \
    --mount=type=bind,source=uv.lock,target=uv.lock \
    --mount=type=bind,source=pyproject.toml,target=pyproject.toml \
    uv sync


FROM base AS dev-server

RUN apt-get update \
    && apt-get install -y --no-install-recommends --no-install-suggests openssh-server rsync

WORKDIR /app
ENV PATH="/app/.venv/bin:$PATH"
ENV PYTHONPATH=/app

CMD ["fastapi", "dev", "app/main.py"]


FROM ${PROXY_CACHE}/python:${PYTHON_VERSION}-slim AS production

ENV PATH="/app/.venv/bin:$PATH" \
    PYTHONPATH=/app \
    PYTHONUNBUFFERED=1 \
    UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy

COPY --from=registry.cytomine.org/github/ghcr.io/astral-sh/uv:0.8.13 /uv /uvx /bin/

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 libglib2.0-0 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

RUN --mount=type=cache,target=/root/.cache/uv \
    --mount=type=bind,source=uv.lock,target=uv.lock \
    --mount=type=bind,source=pyproject.toml,target=pyproject.toml \
    uv sync --frozen --no-install-project

COPY ./pyproject.toml ./uv.lock ./
COPY ./app/ ./app/
COPY ./configs/ ./configs/

RUN --mount=type=cache,target=/root/.cache/uv \
    uv sync

LABEL org.opencontainers.image.authors="uliege@cytomine.org" \
      org.opencontainers.image.url="https://uliege.cytomine.org/" \
      org.opencontainers.image.documentation="https://doc.uliege.cytomine.org/" \
      org.opencontainers.image.source="https://github.com/cytomine/cytomine" \
      org.opencontainers.image.vendor="Cytomine ULiège"

CMD ["fastapi", "run", "app/main.py"]