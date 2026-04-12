# Firewall

Rules are persisted in a database along with the `version` column which is `BIGINT` field
incremented everytime new rule is inserted.

`RuleSyncJob` is scheduled to poll every second to check if new rule is available based on current
in-memory version. If there is new version then `ImmutableRangeMap` is rebuilt (values copied from
the existing one to a new one) and swapped atomically in `RuleEvaluator`.

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
