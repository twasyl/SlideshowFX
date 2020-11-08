FROM alpine:3.12 AS curl-stage

RUN apk update && \
    apk upgrade && \
    apk add curl

FROM curl-stage as main-jdk

RUN mkdir -p /tmp/jdks/main && \
    curl --silent -L https://download.java.net/java/GA/jdk14.0.2/205943a0976c4ed48cb16f1043c5c647/12/GPL/openjdk-14.0.2_linux-x64_bin.tar.gz | tar -xz --strip 1 -C /tmp/jdks/main

FROM curl-stage as build-jdk

RUN mkdir -p /tmp/jdks/build && \
    curl --silent -L https://download.java.net/java/GA/jdk14.0.2/205943a0976c4ed48cb16f1043c5c647/12/GPL/openjdk-14.0.2_linux-x64_bin.tar.gz | tar -xz --strip 1 -C /tmp/jdks/build

FROM ubuntu:18.04

ENV JAVA_HOME /tmp/jdks/main
ENV GRADLE_USER_HOME /home/slideshowfx/.gradle/home

RUN apt update && apt upgrade -y && apt install -y binutils fakeroot && apt autoclean -y && apt autoremove -y && \
    mkdir -p /home/slideshowfx

COPY --from=main-jdk /tmp /tmp
COPY --from=build-jdk /tmp /tmp

WORKDIR /home/slideshowfx

ENTRYPOINT ["./gradlew", "--build-cache", "-Dorg.gradle.java.home=/tmp/jdks/main", "-Pbuild_jdk=/tmp/jdks/build"]