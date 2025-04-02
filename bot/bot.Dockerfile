FROM eclipse-temurin:21

WORKDIR /app
COPY target/bot.jar /app/bot.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "bot.jar" ]
