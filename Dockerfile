FROM eclipse-temurin:17-jdk-alpine
ENV TZ=Asia/Tashkent
WORKDIR /app
COPY  target/*.jar app.jar
