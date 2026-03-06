ARG PROXY_CACHE=registry.cytomine.org/docker
ARG JAVA_VERSION=17-jre
ARG GRADLE_VERSION=8.14-jdk21-alpine

FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION}
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY ./repository/src /app/repository/src
COPY ./repository/build.gradle /app/repository/build.gradle
COPY ./settings.gradle /app/settings.gradle
ENV GRADLE_USER_HOME=/tmp/gradle
COPY ./repository/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD []
