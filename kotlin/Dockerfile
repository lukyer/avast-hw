FROM gradle:6.3-jdk11 as builder
USER root
WORKDIR /builder
ADD . /builder
RUN gradle build --stacktrace

FROM openjdk:11-jre-slim
WORKDIR /app
EXPOSE 8080
COPY --from=builder /builder/build/libs/hw-kotlin-0.0.1-SNAPSHOT.jar .
CMD ["java", "-jar", "hw-kotlin-0.0.1-SNAPSHOT.jar"]