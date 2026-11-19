# Tinder backend

A Java/Spring Boot backend for a dating application. It includes authentication, profiles, discovery, messaging, subscriptions, payment integrations and a Telegram bot. The repository also contains a parking bot module.

## Requirements

- JDK 17
- PostgreSQL and Redis
- Accounts and credentials for the integrations used by the application: Firebase and Google sign-in, object storage, Telegram, email, SMS, Click and face comparison

## Build

```sh
./gradlew clean build
```

On Windows, use `gradlew.bat`. The context-loading test requires the services and configuration described below; to compile without starting those services, run `./gradlew clean assemble`.

## Configuration and running

Provide configuration through environment variables or an external Spring Boot configuration file. Variables used directly by the application are listed in `src/main/resources/application.properties`. Set all required values before starting. Set `GOOGLE_APPLICATION_CREDENTIALS` to the path of a Firebase service-account JSON file outside the repository, or use another supported Google Application Default Credentials source.

Generate a new JWT signing key, for example with `openssl rand -base64 64`, and set `APP_JWT_SECRET_BASE64`. Use a new private key for each environment. Changing it invalidates existing JWT sessions.

Configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATA_REDIS_HOST`, and the Google OAuth client settings as appropriate. Do not commit credentials or real environment files.

```sh
./gradlew bootRun
```

The API documentation is available at `/swagger-ui/index.html` when the application is running. See the controllers for available endpoints.

## Deployment and security

The supplied Dockerfile packages an already-built JAR: run `./gradlew bootJar` before `docker build -t tinder-backend .`. Supply configuration and credentials at runtime rather than adding them to the image.

Review authorization, exposed routes, storage policies and configuration before deploying with real user data or payments. The current S3 initialization configures public object reads; use a dedicated bucket and verify that this is appropriate. Payme credentials are currently empty in code and that integration requires configuration work before use.

## Contributing and license

Open an issue or pull request for a proposed change, and avoid including secrets or personal data. No license has been specified; contact the owner before reusing or redistributing the code.
