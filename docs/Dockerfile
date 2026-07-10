ARG PROXY_CACHE=registry.cytomine.org/docker
ARG NODE_VERSION=22.22.3
ARG NGINX_VERSION=1.22.1

FROM ${PROXY_CACHE}/node:${NODE_VERSION}-alpine AS build

WORKDIR /app
COPY package.json package-lock.json ./

RUN npm ci
COPY code-snippets/ ./code-snippets/
COPY src/ ./src/

RUN npm run build

FROM ${PROXY_CACHE}/nginx:${NGINX_VERSION}-alpine

COPY --from=build /app/src/.vuepress/dist /usr/share/nginx/html
