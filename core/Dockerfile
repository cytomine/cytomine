# List ARGs here for better readability.
ARG CORE_VERSION
ARG CORE_REVISION
ARG JAVA_VERSION=21-jre
ARG GRADLE_VERSION=9.5.1-jdk21
ARG PROXY_CACHE=registry.cytomine.org/docker

#######################################################################################
## Stage: building the core jar file
FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION}-alpine AS jar-builder

ENV GRADLE_USER_HOME=/opt/gradle/.gradle

WORKDIR /app
COPY ./app-engine/build.gradle /app/app-engine/build.gradle
COPY ./app-engine/src /app/app-engine/src
COPY ./common/build.gradle /app/common/build.gradle
COPY ./common/src /app/common/src
COPY ./core/build.gradle /app/core/build.gradle
COPY ./core/src /app/core/src
COPY ./e2e-tests/build.gradle /app/e2e-tests/build.gradle
COPY ./e2e-tests/src /app/e2e-tests/src
COPY ./registry-client/build.gradle /app/registry-client/build.gradle
COPY ./registry-client/src /app/registry-client/src
COPY ./repository/build.gradle /app/repository/build.gradle
COPY ./repository/src /app/repository/src
COPY ./settings.gradle /app/settings.gradle

ARG CORE_VERSION
ENV CORE_VERSION=$CORE_VERSION

RUN sed -i -- 's/version: 0.0.0/version: '$CORE_VERSION'/g' /app/core/src/main/resources/application.yml

RUN --mount=type=cache,target=/opt/gradle/.gradle,id=gradle-cache,sharing=locked \
    gradle :core:bootJar --build-cache --console=verbose

#######################################################################################
## Stage: Cytomine core development image
FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION}-alpine AS dev-server

RUN apk add --no-cache \
    gettext \
    openssh \
    rsync

ENV GRADLE_USER_HOME=/opt/gradle/.gradle

WORKDIR /app

CMD ["/bin/sh"]

#######################################################################################
## Stage: Cytomine core image
FROM ${PROXY_CACHE}/eclipse-temurin:${JAVA_VERSION} AS production

ARG CORE_VERSION
ARG CORE_REVISION
ARG JAVA_VERSION
ARG GRADLE_VERSION

LABEL org.opencontainers.image.authors="uliege@cytomine.org" \
      org.opencontainers.image.url="https://uliege.cytomine.org/" \
      org.opencontainers.image.documentation="https://doc.uliege.cytomine.org/" \
      org.opencontainers.image.source="https://github.com/cytomine/cytomine/tree/main/core" \
      org.opencontainers.image.vendor="Cytomine ULiège" \
      org.opencontainers.image.version="${CORE_VERSION}" \
      org.opencontainers.image.revision="${CORE_REVISION}" \
      org.opencontainers.image.deps.java.distribution="eclipse-temurin" \
      org.opencontainers.image.deps.java.version="${JAVA_VERSION}" \
      org.opencontainers.image.deps.gradle.version="${GRADLE_VERSION}"

ENV DEBIAN_FRONTEND=noninteractive
ENV LANG=C.UTF-8

COPY --from=jar-builder /app/core/build/libs/cytomine.jar /app/cytomine.jar

WORKDIR /app

CMD ["java", "-jar", "/app/cytomine.jar"]
