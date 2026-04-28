# Unreleased

## Breaking

- `Interval` field `time` renamed to `micros`.
- `Interval` field `day` renamed to `days`.
- `Interval` field `month` renamed to `months`.
- The order of fields in `Interval` changed to `months`, `days`, `micros`.
- `Range.Bounded` now has four components: `lower`, `lowerInclusive`, `upper`, `upperInclusive`. Code that previously constructed `new Range.Bounded<>(lower, upper)` must be updated to `Range.bounded(lower, upper)` (canonical `[lower, upper)` form) or `Range.of(lower, lowerInclusive, upper, upperInclusive)` for arbitrary bound combinations.

## Fixes

- Range type codecs (`int4range`, `int8range`, `numrange`, `tsrange`, `tstzrange`, `daterange`) now correctly encode and decode all bound inclusivity combinations (`[`, `(`, `]`, `)`) in both text and binary wire formats. Previously all ranges were treated as canonical `[lower, upper)` and other forms were silently misread or lost.

