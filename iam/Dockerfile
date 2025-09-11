ARG KEYCLOAK_VERSION=24.0.2
ARG PROXY_CACHE=registry.cytomine.org/docker
ARG UBI_VERSION=9.3-1610
ARG IMAGE_VERSION
ARG IMAGE_REVISION

#######################################################################################
## Stage: IAM build
FROM ${PROXY_CACHE}/keycloak/keycloak:${KEYCLOAK_VERSION} AS builder

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure a database vendor
ENV KC_DB=postgres

WORKDIR /opt/keycloak

# Add the provider JAR file to the providers directory
# ADD --chown=keycloak:keycloak <MY_PROVIDER_JAR_URL> /opt/keycloak/providers/myprovider.jar

RUN /opt/keycloak/bin/kc.sh build

#######################################################################################
## Stage: Cytomine IAM dev external tools build
FROM ${PROXY_CACHE}/redhat/ubi9:${UBI_VERSION} AS dev-tools-builder
RUN mkdir -p /mnt/rootfs
RUN dnf install --installroot /mnt/rootfs findutils gettext openssh-server rsync --releasever 9 --setopt install_weak_deps=false --nodocs -y && \
    dnf --installroot /mnt/rootfs clean all && \
    rpm --root /mnt/rootfs -e --nodeps setup

#######################################################################################
## Stage: Cytomine IAM development image
FROM ${PROXY_CACHE}/keycloak/keycloak:${KEYCLOAK_VERSION} AS dev-server

COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY --from=dev-tools-builder /mnt/rootfs /

RUN mkdir /opt/keycloak/data/import
COPY docker/kc_config.json /opt/keycloak/data/import

ENV KC_DB=postgres
ENV KC_HOSTNAME_DEBUG=true
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev", "--import-realm" , "--http-port=8100"]

#######################################################################################
## Stage: Cytomine IAM external tools build
FROM ${PROXY_CACHE}/redhat/ubi9:${UBI_VERSION} AS prod-tools-builder
RUN mkdir -p /mnt/rootfs
RUN dnf install --installroot /mnt/rootfs findutils gettext --releasever 9 --setopt install_weak_deps=false --nodocs -y && \
    dnf --installroot /mnt/rootfs clean all && \
    rpm --root /mnt/rootfs -e --nodeps setup

#######################################################################################
## Stage: Cytomine IAM image
FROM ${PROXY_CACHE}/keycloak/keycloak:${KEYCLOAK_VERSION} AS ci

ARG ENTRYPOINT_SCRIPTS_VERSION
ARG KEYCLOAK_VERSION
ARG IMAGE_VERSION
ARG IMAGE_REVISION

LABEL org.opencontainers.image.authors="dev@cytomine.com" \
      org.opencontainers.image.url="https://www.cytomine.com/" \
      org.opencontainers.image.documentation="https://doc.cytomine.org/" \
      org.opencontainers.image.source="https://github.com/cytomine/Cytomine-IAM" \
      org.opencontainers.image.vendor="Cytomine Corporation SA" \
      org.opencontainers.image.version="${IMAGE_VERSION}" \
      org.opencontainers.image.revision="${IMAGE_REVISION}" \
      org.opencontainers.image.deps.keycloak.version="${KEYCLOAK_VERSION}" \
      org.opencontainers.image.deps.ubi.version="${UBI_VERSION}" \
      org.opencontainers.image.deps.entrypoint.scripts.version="${ENTRYPOINT_SCRIPTS_VERSION}"

COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY --from=prod-tools-builder /mnt/rootfs /

RUN mkdir /opt/keycloak/data/import
COPY docker/kc_config.json /opt/keycloak/data/import

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev" , "--import-realm" , "--http-port=8100"]

#######################################################################################
## Stage: Cytomine IAM image
FROM ${PROXY_CACHE}/keycloak/keycloak:${KEYCLOAK_VERSION} AS production

ARG ENTRYPOINT_SCRIPTS_VERSION
ARG KEYCLOAK_VERSION
ARG IMAGE_VERSION
ARG IMAGE_REVISION
ARG UBI_VERSION

LABEL org.opencontainers.image.authors="dev@cytomine.com" \
      org.opencontainers.image.url="https://www.cytomine.com/" \
      org.opencontainers.image.documentation="https://doc.cytomine.org/" \
      org.opencontainers.image.source="https://github.com/cytomine/Cytomine-IAM" \
      org.opencontainers.image.vendor="Cytomine Corporation SA" \
      org.opencontainers.image.version="${IMAGE_VERSION}" \
      org.opencontainers.image.revision="${IMAGE_REVISION}" \
      org.opencontainers.image.deps.keycloak.version="${KEYCLOAK_VERSION}" \
      org.opencontainers.image.deps.ubi.version="${UBI_VERSION}" \
      org.opencontainers.image.deps.entrypoint.scripts.version="${ENTRYPOINT_SCRIPTS_VERSION}"

COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY --from=prod-tools-builder /mnt/rootfs /

RUN mkdir /opt/keycloak/data/import
COPY docker/kc_config.json /opt/keycloak/data/import

ENV KC_DB=postgres

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start" , "--import-realm"]
