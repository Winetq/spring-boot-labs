FROM maven:3.9.11-eclipse-temurin-21 AS compile

COPY pom.xml pom.xml
COPY src src

RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21 AS package

COPY --from=compile target/*.jar /app/gateway.jar

WORKDIR /app

CMD ["java", "-jar", "gateway.jar"]
