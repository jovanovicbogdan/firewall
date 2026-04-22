# Firewall

Rules are persisted in PostgreSQL database and periodically synced with in-memory data structure
to ensure very fast reads. Threads are scheduled to periodically check if in-memory DS needs to be
refreshed. It will be refreshed if the update was requested from a PgQ listener thread and if
default 10 seconds have passed since the last update.

On app startup, PgQ listener thread is started to listen for an incoming notification from PgQ. A
`PgMonitor` thread is also scheduled to monitor the queue size and it's configured to warn if queue
size exceeds 50%.

## REST

## Add rules

```
POST /api/v1/firewall/rules
Content-Type: application/json

{
  "srcStart": "20.20.20.20",
  "srcEnd": "25.25.25.25",
  "destStart": "30.30.30.30",
  "destEnd": "35.35.35.35",
  "action": "ALLOW"
}
```

## Evaluate rule

```
GET /api/v1/firewall/decision?srcIp=<srcIp>&destIp=<destIp>
```

## Liquibase

### Apply migrations

```shell
./mvnw liquibase:update
```

### Rollback migrations

```shell
./mvnw liquibase:rollback -Dliquibase.rollbackCount=<rollbackCount>
```
