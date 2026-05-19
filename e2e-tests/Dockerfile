ARG PROXY_CACHE=registry.cytomine.org/docker
ARG JAVA_VERSION=21-jre
ARG GRADLE_VERSION=8.14-jdk21-alpine

FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION}
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY ./e2e-tests/src /app/e2e-tests/src
COPY ./e2e-tests/build.gradle /app/e2e-tests/build.gradle
COPY ./settings.gradle /app/settings.gradle
COPY ./app-engine/src/test/resources/artifacts/com.cytomine.dummy.identity.geometry-1.0.0.zip /app/e2e-tests/src/test/resources/com.cytomine.dummy.identity.geometry-1.0.0.zip
COPY ./app-engine/src/test/resources/artifacts/com.cytomine.dummy.identity.image-1.0.0.zip /app/e2e-tests/src/test/resources/com.cytomine.dummy.identity.image-1.0.0.zip
ENV GRADLE_USER_HOME=/tmp/gradle
RUN gradle :e2e-tests:clean :e2e-tests:compileJava :e2e-tests:bootJar :e2e-tests:testClasses --no-daemon --console=verbose
COPY ./e2e-tests/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD []
