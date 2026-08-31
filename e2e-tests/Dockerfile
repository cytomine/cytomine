ARG PROXY_CACHE=registry.cytomine.org/docker
ARG JAVA_VERSION=21-jre
ARG GRADLE_VERSION=8.14-jdk21-alpine

FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION}
RUN apk add --no-cache libstdc++
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
COPY ./app-engine/src/test/resources/artifacts/com.cytomine.dummy.identity.geometry-1.0.0.zip /app/e2e-tests/src/test/resources/com.cytomine.dummy.identity.geometry-1.0.0.zip
COPY ./app-engine/src/test/resources/artifacts/com.cytomine.dummy.identity.image-1.0.0.zip /app/e2e-tests/src/test/resources/com.cytomine.dummy.identity.image-1.0.0.zip
ENV GRADLE_USER_HOME=/tmp/gradle
RUN gradle :e2e-tests:clean :e2e-tests:compileJava :e2e-tests:bootJar :e2e-tests:testClasses --no-daemon --console=verbose
COPY ./e2e-tests/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD []
