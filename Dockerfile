FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:11
EXPOSE 8080:8080
RUN mkdir /app
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.27.0/opentelemetry-javaagent.jar /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/

ENV JAVA_TOOL_OPTIONS "-javaagent:/app/opentelemetry-javaagent.jar -Dotel.exporter.otlp.endpoint=http://172.19.0.1:4317 -Dotel.resource.attributes=service.name=Distributed-tracing-sample"
ENTRYPOINT ["java","-jar","/app/Distributed-tracing-ktor-all.jar"]