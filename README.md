
# Product Service

Product Service is a microservice responsible for managing products and their configurations in the Online Store application.

It provides a REST API for managing products, product option groups and product options.

## Technologies

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Liquibase
- Hibernate
- Maven
- MapStruct
- Lombok
- Springdoc OpenAPI
- Docker and Docker Compose
- JUnit 5, Mockito and Testcontainers
- JaCoCo

## Requirements

To run the application locally, you need:

- Java 21
- Maven
- Docker and Docker Compose (for containerized execution)

## Running the application

### Using Docker Compose

Start the application and PostgreSQL database:

```bash
docker compose up --build
```

The application will be available at:

http://localhost:8080

To stop the application:

```bash
docker compose down
```

### Running locally

Start the PostgreSQL database:

```bash
docker compose up -d product-db
```

Run the application using Maven:

```bash
mvn spring-boot:run
```

The application will be available at:

http://localhost:8080

## API Documentation

Interactive API documentation is available through Swagger UI:

http://localhost:8080/swagger-ui/index.html

OpenAPI specification:

http://localhost:8080/v3/api-docs

## Testing

Run all tests and verify the project:

```bash
mvn clean verify
```

JaCoCo generates a test coverage report in:

```text
target/site/jacoco/index.html
```

The build requires at least 70% instruction coverage.

## CI/CD

GitHub Actions workflows are responsible for:

- Running tests and verifying code coverage on pushes to `stage` and `main`, and on pull requests targeting those branches.
- Building and publishing the Docker image to GitHub Container Registry after a successful push to `main`.

Published Docker image:

```text
ghcr.io/andrzej-kosmowski/product-service:latest
```

## License

This project is currently developed as part of a personal learning project.
