FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY env.sh .
COPY src src

RUN chmod +x ./env.sh
RUN chmod +x ./gradlew

RUN ./env.sh
RUN ./gradlew clean bootJar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

COPY env.sh /app/env.sh

RUN source /app/env.sh

ENTRYPOINT ["sh", "-c", "source /app/env.sh && java -jar app.jar"]
