# Summary

This library ports the Haskell postgresql-types library to Java. It provides optimal lossless representations of PostgreSQL types, codecs for encoding and decoding PostgreSQL types in both text and binary formats accompanied by metadata such as type OIDs and names. The library also includes integration tests to ensure the correctness of the codecs.

For every PostgreSQL type, the library defines a dedicated Java class that provides basic conversion functionality and codec definition.

# JDK type bridge pattern

Domain types in this library wrap PostgreSQL's internal representation (e.g. raw microseconds, scaled integers, bit strings). Where a natural JDK type exists that covers the same concept, the domain class exposes a static factory method `of(JdkType)` and an instance method `toJdkType()` to convert between the two.

Examples:
- `Interval.of(Duration)` / `Interval.toDuration()`
- `Timetz.of(OffsetTime)` / `Timetz.toOffsetTime()`
- `Money.of(BigDecimal, int)` / `Money.toBigDecimal(int)`
- `Bytea.of(byte[])` / `Bytea.toByteBuffer()`, `Bytea.of(ByteBuffer)`
- `Inet.V4.of(Inet4Address)` / `Inet.V4.toInetAddress()`, and equivalents on `Inet.V6`
- `Macaddr.of(byte[])` / `Macaddr.toBytes()`, and equivalently for `Macaddr8`

Rules:
- Use `of(JdkType)` as the factory name when the argument unambiguously identifies the JDK counterpart.
- Use `toJdkType()` (e.g. `toDuration()`, `toOffsetTime()`) as the conversion method name.
- The factory method makes a defensive copy of any mutable input (byte arrays, ByteBuffers).
- If the conversion can lose information (e.g. nanoseconds truncated to microseconds, or a netmask being dropped), document the loss clearly in the Javadoc.
- If the factory accepts values that the domain type cannot represent (e.g. out-of-range longs for an unsigned 32-bit OID), throw `IllegalArgumentException` with a descriptive message.

The library does not cover the type constraints of the PostgreSQL types, such as the length of a varchar or the precision of a numeric.

# References

Use the Haskell library at master (https://github.com/nikita-volkov/postgresql-types) as a reference for tested implementations of standard codecs.

Study its past version (https://github.com/nikita-volkov/postgresql-types/tree/ebadd76c7bc55a3dcc777c89cc404f8ca3c5dbf3) to learn the sketches of a technique of assembling the scalar encoders into composites and arrays. Also study the https://github.com/nikita-volkov/hasql codebase to see a similar but less well abstracted technique, which however is production-tested.

Study https://www.npgsql.org/doc/dev/type-representations.html for a sort of a spec on the binary data format.

# Testing

Use quickcheck (https://github.com/pholser/junit-quickcheck) and testcontainers to test codecs against a real PostgreSQL instance and simulate various edge cases.

# Changelog management

Accumulate the unreleased changes in the `Unreleased` section at the top of the `CHANGELOG.md` file. The release workflow automatically relabels the `Unreleased` section to the new version.

When implementing changes, describe every user-facing change in the changelog. 

Focus on the following categories:

- Non-breaking changes: New features, improvements and optimizations that do not break existing functionality.
- Fixes: Bug fixes and error handling improvements that do not break existing functionality.
- Breaking changes: Changes that break existing functionality, such as API changes, removed features, or changes in behavior. These should be clearly marked and described in detail to help users understand the impact of the change and how to adapt their code if necessary.
