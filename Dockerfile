FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:11
EXPOSE 8080:8080
RUN mkdir /app
COPY ./opentelemetry-javaagent.jar /app/
COPY --from=build /home/gradle/src/build/libs/*.jar /app/
ENV OTEL_LOG_LEVEL=debug
ENV JAVA_TOOL_OPTIONS "-Dotel.exporter.otlp.endpoint=http://172.19.0.1:4317 -Dotel.resource.attributes=service.name=Distributed-tracing-sample"
ENTRYPOINT ["java","-jar","/app/Distributed-tracing-ktor-all.jar"]