FROM gradle:8.13-jdk21 AS build

COPY . /Vk-quote-bot
WORKDIR /Vk-quote-bot

RUN chmod +x env.sh
RUN ./env.sh

RUN gradle clean build

FROM amazoncorretto:21

WORKDIR /app

COPY --from=build /Vk-quote-bot/build/libs/*.jar /app/Vk-quote-bot.jar

CMD ["java", "-jar", "/app/Vk-quote-bot.jar"]
