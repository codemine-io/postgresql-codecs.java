# Unreleased

## Non-breaking

- Added `Interval.of(Duration)` and `Interval.toDuration()` helpers for converting PostgreSQL `interval` values to and from `java.time.Duration` with microsecond precision.
- Added `Timetz.of(OffsetTime)` and `Timetz.toOffsetTime()` helpers for converting PostgreSQL
	`timetz` values to and from `java.time.OffsetTime` with microsecond precision.
- New `Codec.money(int decimals)` factory for constructing a `money` codec with a custom decimal scale (e.g. `0` for Japanese yen).
- Added `Bytea.of(byte[])`, `Bytea.of(ByteBuffer)`, and `Bytea.toByteBuffer()` for converting between `bytea` values and standard JDK byte types.
- Added `Inet.V4.of(Inet4Address)` / `Inet.V4.toInetAddress()` and `Inet.V6.of(Inet6Address)` / `Inet.V6.toInetAddress()` for converting between `inet` values and `java.net.InetAddress`. The netmask is not represented in `InetAddress`; `of` creates a full host address (`/32` or `/128`) and `toInetAddress()` drops the netmask.
- Added `Macaddr.of(byte[])` and `Macaddr.toBytes()` for converting between `macaddr` values and 6-byte arrays.
- Added `Macaddr8.of(byte[])` and `Macaddr8.toBytes()` for converting between `macaddr8` values and 8-byte arrays.

## Breaking

- `Codec.OID` type changed from `Codec<Integer>` to `Codec<Long>`. PostgreSQL OIDs are unsigned 32-bit values spanning 0–4294967295, which exceeds `Integer.MAX_VALUE`. Replace raw `Integer` values with `Long`. `encodeInBinary` throws `IllegalArgumentException` for out-of-range values.
- `Codec.money(int decimals)` now returns `Codec<BigDecimal>` instead of a `Long`.
- `money` codec encoding now validates precision strictly: values must be representable with the configured `decimals` without rounding and within signed 64-bit range, otherwise `IllegalArgumentException` is thrown.
- `Interval` field `time` renamed to `micros`.
- `Interval` field `day` renamed to `days`.
- `Interval` field `month` renamed to `months`.
- The order of fields in `Interval` changed to `months`, `days`, `micros`.
- `Range.Bounded` now has four components: `lower`, `lowerInclusive`, `upper`, `upperInclusive`. Code that previously constructed `new Range.Bounded<>(lower, upper)` must be updated to `Range.bounded(lower, upper)` (canonical `[lower, upper)` form) or `Range.of(lower, lowerInclusive, upper, upperInclusive)` for arbitrary bound combinations.

## Fixes

- Range type codecs (`int4range`, `int8range`, `numrange`, `tsrange`, `tstzrange`, `daterange`) now correctly encode and decode all bound inclusivity combinations (`[`, `(`, `]`, `)`) in both text and binary wire formats. Previously all ranges were treated as canonical `[lower, upper)` and other forms were silently misread or lost.

