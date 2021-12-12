FROM maven:3.8.3-eclipse-temurin-17 AS compile

COPY pom.xml pom.xml
COPY src src

RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-alpine AS package

COPY --from=compile target/*.jar /app/swimmer.jar

WORKDIR /app

CMD ["java", "-jar", "swimmer.jar"]
