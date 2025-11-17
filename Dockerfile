FROM eclipse-temurin:17-jdk-alpine
ENV TZ=Asia/Tashkent
WORKDIR /app
COPY  build/libs/*.jar app.jar
