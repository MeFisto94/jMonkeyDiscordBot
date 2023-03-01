FROM gradle:8.0-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle install --no-daemon

FROM eclipse-temurin:17-jre
ENV DEBIAN_FRONTEND=noninteractive
RUN mkdir /app
COPY --from=build /home/gradle/src/build/install/jMonkeyDiscordBot /app/
WORKDIR /app
CMD ["/app/bin/jMonkeyDiscordBot"]
