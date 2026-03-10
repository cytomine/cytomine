ARG JAVA_VERSION=21-jre
ARG GRADLE_VERSION=8.14-jdk21-alpine
ARG PROXY_CACHE=registry.cytomine.org/docker

FROM ${PROXY_CACHE}/gradle:${GRADLE_VERSION} AS jar-builder
RUN mkdir -p /opt/gradle/.gradle
ENV GRADLE_USER_HOME=/opt/gradle/.gradle

WORKDIR /app
COPY ./settings.gradle /app/settings.gradle
COPY ./common/build.gradle /app/common/build.gradle
COPY ./common/src /app/common/src
COPY ./repository/build.gradle /app/repository/build.gradle
COPY ./repository/src /app/repository/src
RUN gradle :repository:bootJar --no-daemon --console=verbose

FROM ${PROXY_CACHE}/eclipse-temurin:${JAVA_VERSION} AS production
WORKDIR /app
COPY --from=jar-builder /app/repository/build/libs/repository.jar /app/repository.jar

EXPOSE 8081
CMD ["java", "-jar", "/app/repository.jar"]
