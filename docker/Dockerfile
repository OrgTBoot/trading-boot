FROM openjdk:8-jdk-alpine

RUN addgroup -S tboot && adduser -S tboot -G tboot
USER tboot:tboot

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} trading-boot.jar

ENTRYPOINT ["java","-jar","/trading-boot.jar"]