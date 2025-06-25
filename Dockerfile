FROM openjdk:17-jdk-alpine
ENV TZ=Asia/Tashkent
EXPOSE 8080
WORKDIR /app
COPY build/libs/Tinder-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
