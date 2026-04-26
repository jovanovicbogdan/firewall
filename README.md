# Firewall

## Persistence

Rules are persisted in PostgreSQL database. As long as valid action (ALLOW or DENY), source and
destination ranges are provided there are no other constraints when adding new rules. It means that
rule overlaps are also supported. Whenever a rule is inserted, updated or deleted database
trigger will fire (on each statement) and notify PgQ channel about the change. Event though
PostgreSQL performs deduplication of messages, currently, an empty string is sent to `rule_events`
channel.

## Rule sync

PgQ listener thread is started to listen for notifications from PgQ. Dedicated connection is
established for listener (not a pooled connection) and if for some reason connection is lost, an
attempts will be made to re-establish the connection and register listener again. Every 5 seconds
pings are sent to PostgreSQL to keep the connection alive.

PgQ notification thread checks every 3 seconds if there are available notifications. If there are
available notifications it requests in-memory data structure holding all the rules to be rebuilt.

PgQ monitor thread is also scheduled to monitor the queue size and it's configured to warn if
queue size exceeds 50%.

## Rule evaluation

Rules are read from an in-memory `TreeRangeMap` to provide `O(log n)` reads. Throttling is
implemented to avoid potential constant rebuilding of `TreeRangeMap`. If the rule rebuild has been
requested and if since last update passed some time (default 5 seconds) then `TreeRangeMap` will be
refreshed. Also, `TreeRangeMap` is rebuilt unconditionally every 10 minutes.

## REST

## Add rule

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

## Firewall decision

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
