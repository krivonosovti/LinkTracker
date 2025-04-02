FROM eclipse-temurin:21

WORKDIR /app
COPY target/scrapper.jar /app/scrapper.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "scrapper.jar" ]
