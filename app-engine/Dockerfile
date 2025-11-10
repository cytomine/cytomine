# List ARGs here for better readability.
ARG APP_ENGINE_VERSION
ARG APP_ENGINE_REVISION
ARG ENTRYPOINT_SCRIPTS_VERSION=1.4.0
ARG JAVA_VERSION=17-jre
ARG GRADLE_VERSION=7.6.4-jdk17-alpine
ARG GRADLE_DEV_VERSION=7.6.4-jdk17-jammy
ARG PROXY_CACHE=registry.cytomine.org/docker

#######################################################################################
# Stage: dependencies download via gradle
FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION} AS deps-downloader

# We first copy the build.gradle file and the binaries stored in the source repository.
# This way, we retrieve all gradle dependencies at the beginning. All these steps will be
# cached by Docker unless build.gradle. This means that we only retrieve all dependencies
# if we modify the dependencies definition.
RUN mkdir -p /opt/gradle/.gradle
ENV GRADLE_USER_HOME=/opt/gradle/.gradle

WORKDIR /app
COPY ./app-engine/src /app/app-engine/src
COPY ./app-engine/build.gradle /app/app-engine/build.gradle

COPY ./registry-client /app/registry-client
COPY ./settings.gradle /app/settings.gradle

RUN gradle :app-engine:clean :app-engine:compileJava --no-daemon --console=verbose

#######################################################################################
## Stage: building the jar file
FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION} AS jar-builder

ENV GRADLE_USER_HOME=/opt/gradle/.gradle
COPY --from=deps-downloader /opt/gradle/.gradle /opt/gradle/.gradle

WORKDIR /app
COPY ./registry-client /app/registry-client
COPY ./app-engine/src /app/app-engine/src
COPY ./app-engine/build.gradle /app/app-engine/build.gradle
COPY ./settings.gradle /app/settings.gradle

ARG APP_ENGINE_VERSION
ENV APP_ENGINE_VERSION=$APP_ENGINE_VERSION

RUN sed -i -- "s/version: ce-0.0.0/version: ${APP_ENGINE_VERSION}/g" /app/app-engine/src/main/resources/application.yml  && \
    sed -i -- "s/version = 'ce-0.0.0-SNAPSHOT'/version = '${APP_ENGINE_VERSION}'/g" /app/app-engine/build.gradle  && \
    gradle :app-engine:bootJar --console=verbose

#######################################################################################
## Stage: entrypoint script. Use a multi-stage because COPY --from cannot interpolate variables
FROM ${PROXY_CACHE}/cytomine/entrypoint-scripts:${ENTRYPOINT_SCRIPTS_VERSION} AS entrypoint-scripts

#######################################################################################
## Stage: App-Engine development image
FROM ${PROXY_CACHE}/gradle:${GRADLE_DEV_VERSION} AS dev-server

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
      gettext \
      openssh-server \
      rsync

ENV GRADLE_USER_HOME=/opt/gradle/.gradle

# startup scripts
RUN mkdir /docker-entrypoint-cytomine.d/
COPY --from=entrypoint-scripts --chmod=774 /cytomine-entrypoint.sh /usr/local/bin/
COPY --from=entrypoint-scripts --chmod=774 /envsubst-on-templates-and-move.sh /docker-entrypoint-cytomine.d/500-envsubst-on-templates-and-move.sh
COPY --from=entrypoint-scripts --chmod=774 /setup-ssh-dev-env.sh /docker-entrypoint-cytomine.d/1-start-ssh-dev-env.sh

WORKDIR /app
ENTRYPOINT ["cytomine-entrypoint.sh"]

#######################################################################################
## Stage: Cytomine core image
FROM ${PROXY_CACHE}/eclipse-temurin:${JAVA_VERSION} AS production

ARG APP_ENGINE_VERSION
ARG APP_ENGINE_REVISION
ARG ENTRYPOINT_SCRIPTS_VERSION
ARG JAVA_VERSION
ARG GRADLE_VERSION

LABEL org.opencontainers.image.authors="uliege@cytomine.org" \
      org.opencontainers.image.url="https://uliege.cytomine.org/" \
      org.opencontainers.image.documentation="https://doc.cytomine.com/" \
      org.opencontainers.image.source="https://github.com/cytomine/cytomine/tree/main/app-engine" \
      org.opencontainers.image.vendor="Cytomine ULiege" \
      org.opencontainers.image.version="${APP_ENGINE_VERSION}" \
      org.opencontainers.image.revision="${APP_ENGINE_REVISION}" \
      org.opencontainers.image.deps.java.distribution="eclipse-temurin" \
      org.opencontainers.image.deps.java.version="${JAVA_VERSION}" \
      org.opencontainers.image.deps.gradle.version="${GRADLE_VERSION}" \
      org.opencontainers.image.deps.entrypoint.scripts.version="${ENTRYPOINT_SCRIPTS_VERSION}"

ENV DEBIAN_FRONTEND=noninteractive
ENV LANG=C.UTF-8

# base librairies and configuration
COPY --from=jar-builder /app/app-engine/build/libs/cytomine-app-engine.jar /app/cytomine-app-engine.jar

# entrypoint scripts
RUN mkdir /docker-entrypoint-cytomine.d/
COPY --from=entrypoint-scripts --chmod=774 /cytomine-entrypoint.sh /usr/local/bin/
COPY --from=entrypoint-scripts --chmod=774 /envsubst-on-templates-and-move.sh /docker-entrypoint-cytomine.d/500-envsubst-on-templates-and-move.sh

WORKDIR /app
ENTRYPOINT ["cytomine-entrypoint.sh"]

CMD ["java", "-jar", "/app/cytomine-app-engine.jar"]
