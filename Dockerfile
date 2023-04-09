FROM docker.io/bellsoft/liberica-openjdk-alpine:17
WORKDIR /app
RUN apk add nmap curl && mkdir ./config
COPY target/execution-*.jar .
RUN mv *.jar app.jar
EXPOSE 8080/tcp
CMD ["java", "-jar", "app.jar", "-Dserver.port=8080"]