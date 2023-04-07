FROM bellsoft/liberica-openjdk-debian:17
WORKDIR /app
RUN apt install nmap -y && mkdir ./config
COPY target/execution-*.jar .
RUN mv *.jar app.jar
EXPOSE 8080/tcp
CMD ["java", "-jar", "app.jar", "-Dserver.port=8080"]