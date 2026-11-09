ARG JAVA_VERSION=21-jre
ARG GRADLE_VERSION=9.5.1-jdk21-alpine
ARG PROXY_CACHE=registry.cytomine.org/docker

FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION} AS jar-builder
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
COPY ./resource-server/build.gradle /app/resource-server/build.gradle
COPY ./resource-server/src /app/resource-server/src
COPY ./settings.gradle /app/settings.gradle
RUN --mount=type=cache,target=/opt/gradle/.gradle,id=gradle-cache,sharing=locked \
    gradle :resource-server:bootJar --no-daemon --build-cache --console=verbose

FROM ${PROXY_CACHE}/eclipse-temurin:${JAVA_VERSION} AS production
WORKDIR /app
COPY --from=jar-builder /app/resource-server/build/libs/resource-server.jar /app/resource-server.jar

EXPOSE 8082
CMD ["java", "-jar", "/app/resource-server.jar"]
