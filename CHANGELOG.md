# Unreleased

## Non-breaking

- Added `Interval.of(Duration)` and `Interval.toDuration()` helpers for converting PostgreSQL `interval` values to and from `java.time.Duration` with microsecond precision.
- Added `Timetz.of(OffsetTime)` and `Timetz.toOffsetTime()` helpers for converting PostgreSQL
	`timetz` values to and from `java.time.OffsetTime` with microsecond precision.

## Breaking

- `Interval` field `time` renamed to `micros`.
- `Interval` field `day` renamed to `days`.
- `Interval` field `month` renamed to `months`.
- The order of fields in `Interval` changed to `months`, `days`, `micros`.

