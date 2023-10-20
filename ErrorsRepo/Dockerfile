FROM openjdk:22-ea-17-slim
ARG JAR_FILE=target/ErrorsRepo-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

LABEL authors="marcodifiandra"
ENTRYPOINT ["java", "-jar", "/app.jar"]