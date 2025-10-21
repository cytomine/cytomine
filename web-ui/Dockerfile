ARG PROXY_CACHE=registry.cytomine.org/docker
ARG WEB_UI_VERSION
ARG WEB_UI_REVISION
ARG NODE_VERSION=20.19.4
ARG NGINX_VERSION=1.22.1

#######################################################################################
## Stage: build front-end
FROM ${PROXY_CACHE}/node:${NODE_VERSION}-alpine AS frontend-builder

WORKDIR /app

COPY ./package.json /app/package.json
COPY ./package-lock.json /app/package-lock.json

COPY ./src /app/src
COPY ./public /app/public
COPY ./scripts /app/scripts
COPY ./babel.config.js /app/babel.config.js
COPY ./.eslintrc.js /app/.eslintrc.js

RUN npm install && npm run build

#######################################################################################
## Stage: dev image (with hot swap)
FROM ${PROXY_CACHE}/node:${NODE_VERSION}-bookworm-slim AS dev-server

WORKDIR /app

COPY ./scripts/configuration.json ./public/configuration.json

CMD ["npm", "run", "serve"]

#######################################################################################
## Stage: building nginx image serving the front-end
FROM ${PROXY_CACHE}/nginx:${NGINX_VERSION}-alpine AS prod-server

ARG WEB_UI_VERSION
ARG WEB_UI_REVISION
ARG NGINX_VERSION
ARG NODE_VERSION

LABEL org.opencontainers.image.authors="uliege@cytomine.org" \
      org.opencontainers.image.url="https://uliege.cytomine.org/" \
      org.opencontainers.image.documentation="https://doc.uliege.cytomine.org/" \
      org.opencontainers.image.source="https://github.com/cytomine/cytomine/tree/main/web-ui" \
      org.opencontainers.image.vendor="Cytomine ULiège" \
      org.opencontainers.image.version=${WEB_UI_VERSION} \
      org.opencontainers.image.revision=${WEB_UI_REVISION} \
      org.opencontainers.image.deps.nginx.version=${NGINX_VERSION} \
      org.opencontainers.image.deps.node.version=${NODE_VERSION}

WORKDIR /app

COPY ./nginx.conf /etc/nginx/nginx.conf
COPY ./scripts/configuration.json ./configuration.json

COPY --from=frontend-builder /app/dist /app

CMD ["nginx", "-g", "daemon off;"]
