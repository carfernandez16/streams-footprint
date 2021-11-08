FROM openjdk:11

MAINTAINER Carlos Fernandez <croberto.fernandez@gmail.com>

RUN echo "building container for program"

WORKDIR /usr/app

COPY build/libs/app1-all-1.0.0.jar /usr/app/

ENTRYPOINT ["java", "-cp", "app1-all-1.0.0.jar", "org.streams.footprint.App1"]
