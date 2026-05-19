# List ARGs here for better readability.
ARG CORE_VERSION
ARG CORE_REVISION
ARG JAVA_VERSION=21-jre
ARG GRADLE_VERSION=8.14-jdk21
ARG PROXY_CACHE=registry.cytomine.org/docker

#######################################################################################
# Stage: core dependencies download via gradle
FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION}-alpine AS deps-downloader

# We first copy the build.gradle file and the binaries stored in the source repository.
# This way, we retrieve all gradle dependencies at the beginning. All these steps will be
# cached by Docker unless build.gradle. This means that we only retrieve all dependencies
# if we modify the dependencies definition.
RUN mkdir -p /opt/gradle/.gradle
ENV GRADLE_USER_HOME=/opt/gradle/.gradle

WORKDIR /app
COPY ./settings.gradle /app/settings.gradle
COPY ./common/build.gradle /app/common/build.gradle
COPY ./core/build.gradle /app/core/build.gradle

RUN gradle :core:dependencies --no-daemon --console=verbose

#######################################################################################
## Stage: building the core jar file
FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION}-alpine AS jar-builder

ENV GRADLE_USER_HOME=/opt/gradle/.gradle
COPY --from=deps-downloader /opt/gradle/.gradle /opt/gradle/.gradle

WORKDIR /app
COPY ./settings.gradle /app/settings.gradle
COPY ./common/build.gradle /app/common/build.gradle
COPY ./common/src /app/common/src
COPY ./core/src /app/core/src
COPY ./core/build.gradle /app/core/build.gradle

ARG CORE_VERSION
ENV CORE_VERSION=$CORE_VERSION

RUN sed -i -- 's/version: 0.0.0/version: '$CORE_VERSION'/g' /app/core/src/main/resources/application.yml && \
    gradle :core:bootJar --console=verbose

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
