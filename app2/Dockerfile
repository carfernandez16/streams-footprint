FROM amazoncorretto:11

MAINTAINER Carlos Fernandez <croberto.fernandez@gmail.com>

RUN echo "building container for program"

WORKDIR /usr/app

COPY build/libs/app2-all.jar /usr/app/

ENTRYPOINT ["java", "-cp", "app2-all.jar", "org.streams.footprint.App2"]
