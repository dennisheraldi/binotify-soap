FROM maven:3.8-eclipse-temurin-11-alpine AS mvnbuild

WORKDIR /opt/app
COPY pom.xml .
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn -f pom.xml clean package


FROM maven:3.8-eclipse-temurin-11-alpine

WORKDIR /opt/app
COPY --from=mvnbuild /opt/app/target/binotify-jar-with-dependencies.jar binotify.jar
ENTRYPOINT java -jar binotify.jar