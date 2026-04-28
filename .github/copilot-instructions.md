# Summary

This library ports the Haskell postgresql-types library to Java. It provides optimal lossless representations of PostgreSQL types, codecs for encoding and decoding PostgreSQL types in both text and binary formats accompanied by metadata such as type OIDs and names. The library also includes integration tests to ensure the correctness of the codecs.

For every PostgreSQL type, the library defines a dedicated Java class that provides basic conversion functionality and codec definition.

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
