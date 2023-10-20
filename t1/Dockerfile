FROM maven AS build
WORKDIR /app
COPY pom.xml /app
RUN mvn dependency:resolve
COPY . /app
RUN mvn clean
RUN mvn package -DskipTests


FROM openjdk:21-rc-slim
COPY --from=build /app/target/*.jar manvsclass.jar
ENV PORT 8080
EXPOSE 8080
CMD ["java","-jar","manvsclass.jar"]