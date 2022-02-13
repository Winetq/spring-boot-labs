FROM alpine AS clone

RUN apk update &\
    apk add git

RUN git clone -b lab3_gateway https://github.com/Winetq/spring-boot-labs.git

FROM maven:3.8.3-eclipse-temurin-17 AS compile

COPY --from=clone /spring-boot-labs/pom.xml pom.xml
COPY --from=clone /spring-boot-labs/src src

RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-alpine AS package

COPY --from=compile target/*.jar /app/gateway.jar

WORKDIR /app

CMD ["java", "-jar", "gateway.jar"]
