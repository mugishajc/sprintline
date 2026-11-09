# Quarkus Task Manager API

A REST API for managing tasks, built with Quarkus 3. Tasks have a title, description, priority, due date and a status that follows a fixed lifecycle. The list endpoint supports filtering, search, sorting and pagination.

I built it to work with Quarkus end to end: Panache repositories, Bean Validation on resource methods, exception mappers, OpenAPI annotations, health checks and `@QuarkusTest` integration tests.

## Stack

- Java 21, Maven
- Quarkus 3 with Quarkus REST (RESTEasy Reactive) and Jackson
- Hibernate ORM with Panache (repository pattern)
- H2 database (in memory by default)
- Hibernate Validator
- SmallRye OpenAPI with Swagger UI, SmallRye Health
- JUnit 5, RestAssured

## Running it

Requires JDK 21 or newer and Maven 3.9+.

```bash
# dev mode with live reload and five sample tasks
mvn quarkus:dev

# or build and run the packaged app
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```

The API listens on http://localhost:8080.

- Swagger UI: http://localhost:8080/q/swagger-ui
- OpenAPI document: http://localhost:8080/q/openapi
- Health: http://localhost:8080/q/health (also `/q/health/live` and `/q/health/ready`)

Data lives in an in-memory H2 database, so it is gone after a restart. To keep it, point the datasource at a file:

```bash
QUARKUS_DATASOURCE_JDBC_URL=jdbc:h2:file:./data/tasks java -jar target/quarkus-app/quarkus-run.jar
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/tasks` | List tasks (filters, sort and paging below) |
| GET | `/api/tasks/{id}` | Get one task |
| POST | `/api/tasks` | Create a task, returns 201 with a `Location` header |
| PUT | `/api/tasks/{id}` | Replace title, description, priority and due date |
| PATCH | `/api/tasks/{id}/status` | Move a task to another status |
| DELETE | `/api/tasks/{id}` | Delete a task, returns 204 |
| GET | `/api/tasks/stats` | Totals by status, open and overdue counts |

### List parameters

| Parameter | Example | Notes |
|-----------|---------|-------|
| `status` | `IN_PROGRESS` | Case insensitive |
| `priority` | `HIGH` | `LOW`, `MEDIUM`, `HIGH`, `URGENT` |
| `dueBefore`, `dueAfter` | `2026-12-01` | Inclusive, `yyyy-MM-dd` |
| `q` | `invoice` | Searches title and description |
| `overdue` | `true` | Open tasks whose due date has passed |
| `page` | `0` | Zero based |
| `size` | `20` | 1 to 100 |
| `sort` | `-priority,dueDate` | Fields: `title`, `status`, `priority`, `dueDate`, `createdAt`, `updatedAt`. A leading `-` sorts descending |

Priority sorts in business order (URGENT above HIGH), not alphabetically. The entity stores a numeric weight next to the enum name for this.

### Status lifecycle

```
TODO        -> IN_PROGRESS, DONE, CANCELLED
IN_PROGRESS -> TODO, BLOCKED, DONE, CANCELLED
BLOCKED     -> IN_PROGRESS, CANCELLED
DONE        -> IN_PROGRESS   (reopen)
CANCELLED   -> TODO
```

Status is not part of the create or update payload. It only changes through the PATCH endpoint, so the rules above always apply. A transition that is not allowed returns 409. Moving to DONE sets `completedAt`, and reopening clears it.

## Examples

Create a task:

```bash
curl -i -X POST localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Renew TLS certificate","priority":"URGENT","dueDate":"2026-12-01"}'
```

```json
{
  "id": 6,
  "title": "Renew TLS certificate",
  "description": null,
  "status": "TODO",
  "priority": "URGENT",
  "dueDate": "2026-12-01",
  "overdue": false,
  "createdAt": "2026-11-09T08:12:44.120Z",
  "updatedAt": "2026-11-09T08:12:44.120Z",
  "completedAt": null
}
```

Start working on it, then list urgent open work:

```bash
curl -X PATCH localhost:8080/api/tasks/6/status \
  -H 'Content-Type: application/json' -d '{"status":"IN_PROGRESS"}'

curl 'localhost:8080/api/tasks?priority=URGENT&status=IN_PROGRESS&sort=dueDate&size=10'
```

List responses are wrapped in a page object:

```json
{ "items": [ ... ], "page": 0, "size": 10, "totalItems": 1, "totalPages": 1 }
```

Errors from the service come back as JSON:

```json
{ "status": 409, "error": "Conflict", "message": "Cannot move a task from CANCELLED to DONE. Allowed: [TODO]", "timestamp": "..." }
```

Validation errors (blank title, title over 120 characters, page size over 100) return 400 with the list of violations produced by Quarkus.

## Tests

```bash
mvn verify
```

The suite has `@QuarkusTest` integration tests that start the app on a random port and call it with RestAssured, plus plain unit tests for the status rules and the sort parser. Each integration test resets the table first, so they do not depend on order.

## Project layout

```
src/main/java/io/github/mugishajc/tasks
  api/          resource, request and response records, exception mappers
  domain/       Task entity, TaskStatus, TaskPriority
  repository/   Panache repository and the filter record
  service/      business logic, sort parsing, domain exceptions
  health/       readiness check
  config/       Clock producer
```

## License

MIT, see [LICENSE](LICENSE).
