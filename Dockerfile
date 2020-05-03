FROM alpine:3.11 AS curl-stage

RUN apk update && \
    apk upgrade && \
    apk add curl

FROM curl-stage as main-jdk

RUN mkdir -p /tmp/jdks/main && \
    curl --silent -L https://download.java.net/java/GA/jdk14.0.1/664493ef4a6946b186ff29eb326336a2/7/GPL/openjdk-14.0.1_linux-x64_bin.tar.gz | tar -xz --strip 1 -C /tmp/jdks/main

FROM curl-stage as build-jdk

RUN mkdir -p /tmp/jdks/build && \
    curl --silent -L https://download.java.net/java/GA/jdk14.0.1/664493ef4a6946b186ff29eb326336a2/7/GPL/openjdk-14.0.1_linux-x64_bin.tar.gz | tar -xz --strip 1 -C /tmp/jdks/build

FROM ubuntu:19.10

ENV JAVA_HOME /tmp/jdks/main
ENV GRADLE_USER_HOME /slideshowfx/.gradle/home

RUN apt update && apt upgrade -y && apt install -y binutils fakeroot && apt autoclean -y && apt autoremove -y

COPY --from=main-jdk /tmp /tmp
COPY --from=build-jdk /tmp /tmp

WORKDIR /slideshowfx

ENTRYPOINT ["./gradlew", "--build-cache", "-Dorg.gradle.java.home=/tmp/jdks/main", "-Pbuild_jdk=/tmp/jdks/build"]