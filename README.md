# Firewall

On app startup `PgQWorker` listener threads are started to listen for incoming notifications from
PgQ. Whenever rule is inserted, updated or deleted a trigger is fired to signal an event
`rule_changed`. This event requests that in-memory `ImmutableRangeMap` should be rebuilt in
`RuleEvaluator` class. To avoid constant rebuilding, throttling is implemented, the time between
rebuilding has to be at least 5 seconds.

## Message Delivery

From PostgreSQL [docs](https://www.postgresql.org/docs/current/sql-notify.html):

> PgQ `NOTIFY` docs states if the same channel name is signaled multiple times with
identical payload strings within the same transaction, only one instance of the notification event
is delivered to listeners. NOTIFY guarantees that notifications from the same transaction get
delivered in the order they were sent. It is also guaranteed that messages from different
transactions are delivered in the order in which the transactions committed.

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
