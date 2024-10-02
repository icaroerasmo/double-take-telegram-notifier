FROM maven:3.8.8-amazoncorretto-21 AS build
WORKDIR /app
COPY src ./src/
COPY pom.xml .
RUN mvn clean package

FROM amazoncorretto:21-alpine
COPY --from=build /app/target/double-take-telegram-notifier-*.jar /app/double-take-telegram-notifier.jar
ENTRYPOINT [ "java", "-Dspring.config.additional-location=optional:/app/config/config.yaml", "-jar", "/app/double-take-telegram-notifier.jar" ]
