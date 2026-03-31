# postgresql-codecs

[![Docs](https://img.shields.io/badge/Docs-all-white)](https://codemine.io/postgresql-codecs.java/)
[![Maven](https://img.shields.io/badge/Maven-latest-blue)](https://codemine.io/postgresql-codecs.java/latest/)
[![Javadoc](https://img.shields.io/badge/Javadoc-latest-green)](https://codemine.io/postgresql-codecs.java/latest/apidocs/)

A driver-agnostic Java library for encoding and decoding PostgreSQL data types in both text and binary wire formats.

## Motivation

The common JDBC and R2DBC drivers don't support many standard PostgreSQL types. Their support for composite types is virtually missing.

This library provides:

- Lossless, type-safe Java representations of all standard PostgreSQL types (numeric, temporal, geometric, network, binary, JSON, composite, enum, arrays, ranges, multiranges).
- `Codec<A>` — a single interface that handles textual *and* binary wire formats, carries the PostgreSQL type OID, and composes into arrays and composite types.
- Complete decoupling from any specific driver: the same codec is compatible with pgjdbc, r2dbc-postgresql, or any other PostgreSQL client.

## Quality assurance

Every codec is heavily tested against a live PostgreSQL instance using both pgjdbc and r2dbc-postgresql. The tests generate random values, encode them in binary and text formats, execute a `SELECT ?::type` query, and verify that the returned value is identical to the original deserializing them in both binary and text formats. Thus they cover all possible combinations of serialization and deserialization paths: binary-to-binary, binary-to-text, text-to-binary, and text-to-text.

## Installation

The package is published to GitHub Packages under `io.codemine.java.postgresql:codecs`.

```xml
<dependency>
    <groupId>io.codemine.java.postgresql</groupId>
    <artifactId>codecs</artifactId>
    <version>0.1.0</version>
</dependency>
```

Add the GitHub Packages repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/codemine-io/postgresql-codecs.java</url>
    </repository>
</repositories>
```

## Supported types

| PostgreSQL type | Java Lib | Java type | Codec |
|---|---|---|---|
| `bool` | std | `Boolean` | `Codec.BOOL` |
| `int2` | std | `Short` | `Codec.INT2` |
| `int4` | std | `Integer` | `Codec.INT4` |
| `int8` | std | `Long` | `Codec.INT8` |
| `float4` | std | `Float` | `Codec.FLOAT4` |
| `float8` | std | `Double` | `Codec.FLOAT8` |
| `numeric` | std | `BigDecimal` | `Codec.NUMERIC` |
| `text` | std | `String` | `Codec.TEXT` |
| `varchar` | std | `String` | `Codec.VARCHAR` |
| `bpchar` | std | `String` | `Codec.BPCHAR` |
| `char` | std | `Byte` | `Codec.CHAR` |
| `bytea` | postgresql-codecs | `Bytea` | `Codec.BYTEA` |
| `uuid` | std | `UUID` | `Codec.UUID` |
| `json` | jackson | `JsonNode` | `Codec.JSON` |
| `jsonb` | jackson | `JsonNode` | `Codec.JSONB` |
| `oid` | std | `Integer` | `Codec.OID` |
| `money` | std | `Long` | `Codec.MONEY` |
| `date` | std | `LocalDate` | `Codec.DATE` |
| `time` | std | `LocalTime` | `Codec.TIME` |
| `timetz` | postgresql-codecs | `Timetz` | `Codec.TIMETZ` |
| `timestamp` | std | `LocalDateTime` | `Codec.TIMESTAMP` |
| `timestamptz` | std | `Instant` | `Codec.TIMESTAMPTZ` |
| `interval` | postgresql-codecs | `Interval` | `Codec.INTERVAL` |
| `inet` | postgresql-codecs | `Inet` | `Codec.INET` |
| `cidr` | postgresql-codecs | `Cidr` | `Codec.CIDR` |
| `macaddr` | postgresql-codecs | `Macaddr` | `Codec.MACADDR` |
| `macaddr8` | postgresql-codecs | `Macaddr8` | `Codec.MACADDR8` |
| `point` | postgresql-codecs | `Point` | `Codec.POINT` |
| `line` | postgresql-codecs | `Line` | `Codec.LINE` |
| `lseg` | postgresql-codecs | `Lseg` | `Codec.LSEG` |
| `box` | postgresql-codecs | `Box` | `Codec.BOX` |
| `path` | postgresql-codecs | `Path` | `Codec.PATH` |
| `polygon` | postgresql-codecs | `Polygon` | `Codec.POLYGON` |
| `circle` | postgresql-codecs | `Circle` | `Codec.CIRCLE` |
| `bit` | postgresql-codecs | `Bit` | `Codec.BIT` |
| `varbit` | postgresql-codecs | `Bit` | `Codec.VARBIT` |
| `citext` | std | `String` | `Codec.CITEXT` |
| `tsvector` | postgresql-codecs | `Tsvector` | `Codec.TSVECTOR` |
| `hstore` | postgresql-codecs | `Hstore` | `Codec.HSTORE` |
| `int4range` | postgresql-codecs | `Range<Integer>` | `Codec.INT4RANGE` |
| `int8range` | postgresql-codecs | `Range<Long>` | `Codec.INT8RANGE` |
| `numrange` | postgresql-codecs | `Range<BigDecimal>` | `Codec.NUMRANGE` |
| `tsrange` | postgresql-codecs | `Range<LocalDateTime>` | `Codec.TSRANGE` |
| `tstzrange` | postgresql-codecs | `Range<Instant>` | `Codec.TSTZRANGE` |
| `daterange` | postgresql-codecs | `Range<LocalDate>` | `Codec.DATERANGE` |
| `int4multirange` | postgresql-codecs | `Multirange<Integer>` | `Codec.INT4MULTIRANGE` |
| `int8multirange` | postgresql-codecs | `Multirange<Long>` | `Codec.INT8MULTIRANGE` |
| `nummultirange` | postgresql-codecs | `Multirange<BigDecimal>` | `Codec.NUMMULTIRANGE` |
| `tsmultirange` | postgresql-codecs | `Multirange<LocalDateTime>` | `Codec.TSMULTIRANGE` |
| `tstzmultirange` | postgresql-codecs | `Multirange<Instant>` | `Codec.TSTZMULTIRANGE` |
| `datemultirange` | postgresql-codecs | `Multirange<LocalDate>` | `Codec.DATEMULTIRANGE` |

Any scalar codec can be promoted to array codec of any dimensionality. E.g.:

```java
Codec<List<Integer>> int4Array1D = Codec.INT4.inDim();
Codec<List<List<Integer>>> int4Array2D = Codec.INT4.inDim().inDim();
```

Custom types can be mapped without losing binary support:

```java
Codec<MyId> myIdCodec = Codec.INT4.map(MyId::new, MyId::value);
```

### Composite type example

Given a PostgreSQL type `CREATE TYPE inventory_item AS (name text, qty int4)`:

```java
record InventoryItem(String name, int qty) {}

Codec<InventoryItem> itemCodec = new CompositeCodec<>(
    "",                                               // schema (empty = default search path)
    "inventory_item",                                 // PostgreSQL type name
    (String name) -> (Integer qty) -> new InventoryItem(name, qty),
    new CompositeCodec.Field<>("name", InventoryItem::name, Codec.TEXT),
    new CompositeCodec.Field<>("qty",  InventoryItem::qty,  Codec.INT4));
```

Use it exactly like any other codec. Array support is automatic:

```java
Codec<List<InventoryItem>> itemArrayCodec = itemCodec.inDim();
```

### Enum type example

Given a PostgreSQL type `CREATE TYPE status AS ENUM ('pending', 'active', 'closed')`:

```java
enum Status { PENDING, ACTIVE, CLOSED }

Codec<Status> statusCodec = new EnumCodec<>(
    "",        // schema
    "status",  // PostgreSQL type name
    Map.of(
        Status.PENDING, "pending",
        Status.ACTIVE,  "active",
        Status.CLOSED,  "closed"));
```

### Domain type example

Given `CREATE DOMAIN email AS text CHECK (VALUE ~ '^[^@]+@[^@]+$')`:

```java
Codec<String> emailCodec = Codec.TEXT.withType("", "email");
```

The resulting codec encodes and decodes `String` values exactly like `Codec.TEXT`, but reports
`"email"` as its type name so that the driver sends the correct type annotation. If you look up the
domain's OIDs at startup you can supply them for full binary-format support:

```java
// Look up OIDs at application startup, e.g.:
int scalarOid;
int arrayOid;
try (var ps = conn.prepareStatement(
        "SELECT oid::int, typarray::int FROM pg_type WHERE typname = 'email'");
     var rs = ps.executeQuery()) {
    rs.next();
    scalarOid = rs.getInt(1);
    arrayOid  = rs.getInt(2);
}

Codec<String> emailCodec = Codec.TEXT.withType("", "email", scalarOid, arrayOid);
```

## Usage with JDBC (pgjdbc)

Use `Codec.encodeInTextToString` to encode a value as a PostgreSQL text literal and wrap it in a `PGobject`. Use `Codec.decodeInTextFromString` to decode the text column returned by the driver.

```java
import io.codemine.java.postgresql.codecs.Codec;
import org.postgresql.util.PGobject;

// --- Encoding ---
Codec<Integer> codec = Codec.INT4;

PGobject obj = new PGobject();
obj.setType(codec.typeSig());          // e.g. "int4"
obj.setValue(codec.encodeInTextToString(42));

PreparedStatement ps = connection.prepareStatement("INSERT INTO t (col) VALUES (?)");
ps.setObject(1, obj);
ps.executeUpdate();

// --- Decoding ---
PreparedStatement q = connection.prepareStatement("SELECT col FROM t WHERE id = ?");
q.setInt(1, 1);
ResultSet rs = q.executeQuery();
if (rs.next()) {
    Integer value = codec.decodeInTextFromString(rs.getString("col"));
}
```

Array columns work identically — just use the array codec:

```java
Codec<List<Integer>> arrayCodec = Codec.INT4.inDim();

PGobject arr = new PGobject();
arr.setType(arrayCodec.typeSig());     // "int4[]"
arr.setValue(arrayCodec.encodeInTextToString(List.of(1, 2, 3)));
```

## Usage with R2DBC (r2dbc-postgresql)

In [`./src/test/java/io/codemine/postgresql/BinaryInBinaryOutR2dbcCodec.java`](./src/test/java/io/codemine/postgresql/BinaryInBinaryOutR2dbcCodec.java) you'll find an adapter that integrates `Codec<A>` with r2dbc-postgresql's `Codec` interface using binary serialization format. You can bundle it into your codebase and register using `codecRegistrar`.

```java
import io.codemine.postgresql.BinaryInBinaryOutR2dbcCodec;
import io.codemine.java.postgresql.codecs.Codec;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import reactor.core.publisher.Mono;

var config = PostgresqlConnectionConfiguration.builder()
    .host("localhost")
    .port(5432)
    .username("user")
    .password("secret")
    .database("mydb")
    .forceBinary(true)
    .codecRegistrar((connection, allocator, registry) -> {
        registry.addFirst(new BinaryInBinaryOutR2dbcCodec<>(Codec.INT4, Integer.class));
        return Mono.empty();
    })
    .build();

var factory = new PostgresqlConnectionFactory(config);

// Roundtrip example
Mono.from(factory.create())
    .flatMapMany(conn ->
        conn.createStatement("SELECT $1::int4")
            .bind(0, 42)
            .execute()
    )
    .flatMap(result -> result.map((row, meta) -> row.get(0, Integer.class)))
    .subscribe(System.out::println);
```

For connections without `forceBinary`, you can use [the adapter in Text format](./src/test/java/io/codemine/postgresql/TextInTextOutR2dbcCodec.java) instead.
