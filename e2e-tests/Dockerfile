ARG PROXY_CACHE=registry.cytomine.org/docker
ARG JAVA_VERSION=17-jre
ARG GRADLE_VERSION=8.14-jdk21-alpine

FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION}
RUN apk add --no-cache libstdc++ firefox geckodriver
WORKDIR /app
COPY ./e2e-tests/src /app/e2e-tests/src
COPY ./e2e-tests/build.gradle /app/e2e-tests/build.gradle
COPY ./settings.gradle /app/settings.gradle
ENV GRADLE_USER_HOME=/tmp/gradle
RUN gradle :e2e-tests:clean :e2e-tests:compileJava :e2e-tests:bootJar :e2e-tests:testClasses --no-daemon --console=verbose
COPY ./e2e-tests/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD []
