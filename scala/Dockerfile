# TODO fix builder with fatjar

#FROM mozilla/sbt:LATEST as builder
#USER root
#WORKDIR /builder
#ADD . /builder
#RUN sbt compile

FROM openjdk:11-jre-slim
WORKDIR /app
EXPOSE 8980
#COPY --from=builder /builder/target/ .
ADD . .
#RUN ./sbt clean compile
CMD ["./sbt", "run"]