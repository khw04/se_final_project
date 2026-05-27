# Pokemo Backend

Spring Boot REST API scaffold for Pokemo.

## Commands

```cmd
mvnw.cmd test
mvnw.cmd spring-boot:run
```

`spring-boot:run` uses the local H2 in-memory profile by default so the API can be tested without MySQL.
Use `SPRING_PROFILES_ACTIVE=dev` when testing against a local MySQL database.

## Endpoints

- Actuator health: `http://localhost:8080/actuator/health`
- API health: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Profiles

- `dev`: local MySQL defaults with environment variable overrides
- `local`: in-memory H2 database for quick API testing
- `prod`: environment-variable-only production settings

Set `SPRING_PROFILES_ACTIVE=dev` for MySQL-backed local development.
