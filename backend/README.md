# Pokemo Backend

Spring Boot REST API scaffold for Pokemo.

## Commands

```cmd
mvnw.cmd test
mvnw.cmd spring-boot:run
```

## Endpoints

- Actuator health: `http://localhost:8080/actuator/health`
- API health: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Profiles

- `dev`: local MySQL defaults with environment variable overrides
- `prod`: environment-variable-only production settings

Set `SPRING_PROFILES_ACTIVE=dev` for local development.
