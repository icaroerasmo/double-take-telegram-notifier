FROM maven:3.8.8-amazoncorretto-21 AS build
WORKDIR /app
COPY src ./src/
COPY pom.xml .
RUN mvn clean package

FROM amazoncorretto:21-alpine
COPY --from=build /app/target/double-take-telegram-notifier-*.jar /usr/local/lib/double-take-telegram-notifier.jar
ENTRYPOINT [ "java", "-jar", "/usr/local/lib/double-take-telegram-notifier.jar" ]
