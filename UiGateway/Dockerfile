FROM openjdk:22-ea-17-slim
LABEL authors="marcodifiandra"
RUN apt update && apt install -y bash
RUN apt update && apt install -y openssl libncurses5 libstdc++6

COPY target/UiGateway-0.0.1-SNAPSHOT.jar /app/uiGateway.jar

EXPOSE 8080
CMD ["java","-jar","uiGateway.jar"]
WORKDIR /app

