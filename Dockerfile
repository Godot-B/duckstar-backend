FROM public.ecr.aws/docker/library/gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

FROM public.ecr.aws/docker/library/eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENV TZ=Asia/Seoul
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
