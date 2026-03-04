ARG PROXY_CACHE=registry.cytomine.org/docker
ARG JAVA_VERSION=17-jdk
ARG GRADLE_VERSION=8.14-jdk21-alpine

FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION} AS builder
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY ./e2e-tests/src /app/e2e-tests/src
COPY ./e2e-tests/build.gradle /app/e2e-tests/build.gradle
COPY ./settings.gradle /app/settings.gradle
ENV GRADLE_USER_HOME=/gradle-cache
RUN gradle :e2e-tests:testClasses --no-daemon --console=verbose

FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION}
WORKDIR /app
COPY --from=builder /app /app
COPY --from=builder /gradle-cache /gradle-cache
ENV GRADLE_USER_HOME=/gradle-cache
COPY ./e2e-tests/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD []
