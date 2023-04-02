FROM bellsoft/liberica-openjdk-alpine:17
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN apk add nmap
RUN ./mvnw dependency:resolve
RUN mkdir ./config
COPY src ./src
EXPOSE 8080/tcp
CMD ["./mvnw", "spring-boot:run", "-Dserver.port=8080"]