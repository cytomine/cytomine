ARG PROXY_CACHE=registry.cytomine.org/docker
ARG WEB_UI_VERSION
ARG WEB_UI_REVISION
ARG NODE_VERSION=20.19.4
ARG NGINX_VERSION=1.22.1

#######################################################################################
## Stage: build front-end
FROM ${PROXY_CACHE}/node:${NODE_VERSION}-alpine AS frontend-builder

WORKDIR /app

COPY ./lerna.json /app/lerna.json
COPY ./package.json /app/package.json
COPY ./package-lock.json /app/package-lock.json

COPY ./ui/src /app/ui/src
COPY ./ui/public /app/ui/public
COPY ./ui/scripts /app/ui/scripts
COPY ./ui/package.json /app/ui/package.json
COPY ./ui/babel.config.js /app/ui/babel.config.js
COPY ./ui/.eslintrc.js /app/ui/.eslintrc.js

COPY ./api-client/src /app/api-client/src
COPY ./api-client/package.json /app/api-client/package.json
COPY ./api-client/webpack.config.js /app/api-client/webpack.config.js

RUN npm install && npm run build -w ui

#######################################################################################
## Stage: dev image (with hot swap)
FROM ${PROXY_CACHE}/node:${NODE_VERSION}-bookworm-slim AS dev-server

WORKDIR /app

COPY ./ui/scripts/configuration.json ./ui/public/configuration.json

CMD ["npm", "run", "serve", "-w", "ui"]

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

COPY ./ui/docker/configs/nginx.conf /etc/nginx/nginx.conf
COPY ./ui/scripts/configuration.json ./configuration.json

COPY --from=frontend-builder /app/ui/dist /app

CMD ["nginx", "-g", "daemon off;"]
