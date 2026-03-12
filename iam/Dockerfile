ARG KEYCLOAK_VERSION=24.0.2
ARG PROXY_CACHE=registry.cytomine.org/docker
ARG UBI_VERSION=9.3-1610
ARG IMAGE_VERSION
ARG IMAGE_REVISION

#######################################################################################
## Stage: IAM build
FROM ${PROXY_CACHE}/keycloak/keycloak:${KEYCLOAK_VERSION} AS builder

ENV KC_DB=postgres \
    KC_FEATURES=token-exchange \
    KC_HEALTH_ENABLED=true \
    KC_HTTP_RELATIVE_PATH=/iam \
    KC_METRICS_ENABLED=true

WORKDIR /opt/keycloak

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
COPY kc_config.json /opt/keycloak/data/import

ENV KC_HTTP_RELATIVE_PATH=/iam

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev", "--import-realm", "--http-port=8100"]

#######################################################################################
## Stage: Cytomine IAM external tools build
FROM ${PROXY_CACHE}/redhat/ubi9:${UBI_VERSION} AS prod-tools-builder
RUN mkdir -p /mnt/rootfs
RUN dnf install --installroot /mnt/rootfs findutils gettext --releasever 9 --setopt install_weak_deps=false --nodocs -y && \
    dnf --installroot /mnt/rootfs clean all && \
    rpm --root /mnt/rootfs -e --nodeps setup

#######################################################################################
## Stage: Cytomine IAM image
FROM ${PROXY_CACHE}/keycloak/keycloak:${KEYCLOAK_VERSION} AS production

ARG KEYCLOAK_VERSION
ARG IMAGE_VERSION
ARG IMAGE_REVISION
ARG UBI_VERSION

LABEL org.opencontainers.image.authors="uliege@cytomine.org" \
    org.opencontainers.image.url="https://uliege.cytomine.org/" \
    org.opencontainers.image.documentation="https://doc.uliege.cytomine.org/" \
    org.opencontainers.image.source="https://github.com/cytomine/cytomine/tree/main/iam" \
    org.opencontainers.image.vendor="Cytomine ULiège" \
    org.opencontainers.image.version="${IMAGE_VERSION}" \
    org.opencontainers.image.revision="${IMAGE_REVISION}" \
    org.opencontainers.image.deps.keycloak.version="${KEYCLOAK_VERSION}" \
    org.opencontainers.image.deps.ubi.version="${UBI_VERSION}"

COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY --from=prod-tools-builder /mnt/rootfs /

RUN mkdir /opt/keycloak/data/import
COPY kc_config.json /opt/keycloak/data/import

ENV KC_HTTP_PORT=8100

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start" , "--import-realm", "--optimized"]
