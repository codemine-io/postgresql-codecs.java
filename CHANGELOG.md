# Unreleased

## Non-breaking

- Added `Interval.of(Duration)` and `Interval.toDuration()` helpers for converting PostgreSQL `interval` values to and from `java.time.Duration` with microsecond precision.
- Added `Timetz.of(OffsetTime)` and `Timetz.toOffsetTime()` helpers for converting PostgreSQL
	`timetz` values to and from `java.time.OffsetTime` with microsecond precision.
- New `Money` record type wrapping a raw `long amount`. Includes `Money.of(BigDecimal, int decimals)` and `Money.toBigDecimal(int decimals)` for lossless conversion with an explicit scale.
- New `Codec.money(int decimals)` factory for constructing a `money` codec with a custom decimal scale (e.g. `0` for Japanese yen).

## Breaking

- `Codec.MONEY` type changed from `Codec<Long>` to `Codec<Money>`. Replace raw `Long` values with `Money` and use `Money.of`/`toBigDecimal` for `BigDecimal` interop.
- `Interval` field `time` renamed to `micros`.
- `Interval` field `day` renamed to `days`.
- `Interval` field `month` renamed to `months`.
- The order of fields in `Interval` changed to `months`, `days`, `micros`.

