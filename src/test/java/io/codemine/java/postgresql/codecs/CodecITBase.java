package io.codemine.java.postgresql.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.codemine.java.postgresql.BinaryInBinaryOutR2dbcCodec;
import io.codemine.java.postgresql.BinaryInTextOutR2dbcCodec;
import io.codemine.java.postgresql.TextInBinaryOutR2dbcCodec;
import io.codemine.java.postgresql.TextInTextOutR2dbcCodec;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.postgresql.util.PGobject;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class CodecITBase<A> {

  /**
   * Cache of shared R2DBC connections keyed by concrete test class. Declared before the static
   * initialiser so that the JVM shutdown hook registered there can reference it without an illegal
   * forward-reference compiler error.
   */
  private static final ConcurrentHashMap<Class<?>, SharedConnections> sharedConnectionsByClass =
      new ConcurrentHashMap<>();

  static final PostgreSQLContainer<?> container;
  static final HikariDataSource jdbcPool;

  static {
    container =
        new PostgreSQLContainer<>("postgres:18").withCommand("postgres -c max_connections=500");
    container.start();

    var hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(container.getJdbcUrl());
    hikariConfig.setUsername(container.getUsername());
    hikariConfig.setPassword(container.getPassword());
    // Disable server-side prepared-statement caching so that all result
    // columns remain in text format (avoids rs.getString() returning
    // "[B@…" for bytea columns after the binary-mode switch threshold).
    hikariConfig.addDataSourceProperty("prepareThreshold", "0");
    hikariConfig.setMaximumPoolSize(10);
    jdbcPool = new HikariDataSource(hikariConfig);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  sharedConnectionsByClass
                      .values()
                      .forEach(
                          conns -> {
                            try {
                              conns.close();
                            } catch (Exception ignored) {
                            }
                          });
                  jdbcPool.close();
                }));
  }

  /**
   * Holds all shared connections for a single concrete test class. Created once per subclass and
   * cached in {@link #sharedConnectionsByClass} so that jqwik's per-property instance creation does
   * not open fresh connections on each instantiation.
   */
  private static class SharedConnections {
    final Connection binaryInBinaryOutConn;
    final Connection textInTextOutConn;
    final Connection textInBinaryOutConn;
    final Connection binaryInTextOutConn;
    final Connection arrayArrayBinaryInBinaryOutConn;
    final Connection arrayArrayTextInTextOutConn;
    final Connection arrayArrayTextInBinaryOutConn;
    final Connection arrayArrayBinaryInTextOutConn;

    SharedConnections(
        Connection binaryInBinaryOutConn,
        Connection textInTextOutConn,
        Connection textInBinaryOutConn,
        Connection binaryInTextOutConn,
        Connection arrayArrayBinaryInBinaryOutConn,
        Connection arrayArrayTextInTextOutConn,
        Connection arrayArrayTextInBinaryOutConn,
        Connection arrayArrayBinaryInTextOutConn) {
      this.binaryInBinaryOutConn = binaryInBinaryOutConn;
      this.textInTextOutConn = textInTextOutConn;
      this.textInBinaryOutConn = textInBinaryOutConn;
      this.binaryInTextOutConn = binaryInTextOutConn;
      this.arrayArrayBinaryInBinaryOutConn = arrayArrayBinaryInBinaryOutConn;
      this.arrayArrayTextInTextOutConn = arrayArrayTextInTextOutConn;
      this.arrayArrayTextInBinaryOutConn = arrayArrayTextInBinaryOutConn;
      this.arrayArrayBinaryInTextOutConn = arrayArrayBinaryInTextOutConn;
    }

    void close() {
      Mono.from(binaryInBinaryOutConn.close())
          .then(Mono.from(textInTextOutConn.close()))
          .then(Mono.from(textInBinaryOutConn.close()))
          .then(Mono.from(binaryInTextOutConn.close()))
          .then(Mono.from(arrayArrayBinaryInBinaryOutConn.close()))
          .then(Mono.from(arrayArrayTextInTextOutConn.close()))
          .then(Mono.from(arrayArrayTextInBinaryOutConn.close()))
          .then(Mono.from(arrayArrayBinaryInTextOutConn.close()))
          .block();
    }
  }

  private final Codec<A> codec;
  private final Class<A> type;

  private final Codec<List<A>> arrayCodec;
  private final Codec<List<List<A>>> arrayArrayCodec;

  /**
   * Persistent R2DBC connection whose codec sends parameters in <b>binary</b> format and expects
   * results in <b>binary</b> format ({@code forceBinary=true}). Handles both scalar and array
   * values.
   */
  private final Connection binaryInBinaryOutConn;

  /**
   * Persistent R2DBC connection whose codec sends parameters in <b>text</b> format and expects
   * results in <b>text</b> format (no {@code forceBinary}). Handles both scalar and array values.
   */
  private final Connection textInTextOutConn;

  /**
   * Persistent R2DBC connection whose codec sends parameters in <b>text</b> format and expects
   * results in <b>binary</b> format ({@code forceBinary=true}). Handles both scalar and array
   * values.
   */
  private final Connection textInBinaryOutConn;

  /**
   * Persistent R2DBC connection whose codec sends parameters in <b>binary</b> format and expects
   * results in <b>text</b> format (no {@code forceBinary}). Handles both scalar and array values.
   */
  private final Connection binaryInTextOutConn;

  /** Dedicated R2DBC connections for nested array roundtrips. */
  private final Connection arrayArrayBinaryInBinaryOutConn;

  private final Connection arrayArrayTextInTextOutConn;
  private final Connection arrayArrayTextInBinaryOutConn;
  private final Connection arrayArrayBinaryInTextOutConn;

  @SuppressWarnings("unchecked")
  protected CodecITBase(Codec<A> codec, Class<A> type) {
    this.codec = codec;
    this.type = type;
    this.arrayCodec = codec.inDim();
    this.arrayArrayCodec = arrayCodec.inDim();

    // Retrieve or create shared R2DBC connections for this concrete subclass.
    // computeIfAbsent ensures that even when jqwik instantiates the class
    // multiple times (once per @Property), we only ever open the connections
    // once. Connections are closed by the JVM shutdown hook registered in the
    // static initialiser rather than in @AfterAll, so they are never removed
    // from the map during the test run and are therefore never re-created.
    SharedConnections conns =
        sharedConnectionsByClass.computeIfAbsent(
            this.getClass(), cls -> createSharedConnections(codec, type));

    binaryInBinaryOutConn = conns.binaryInBinaryOutConn;
    textInTextOutConn = conns.textInTextOutConn;
    textInBinaryOutConn = conns.textInBinaryOutConn;
    binaryInTextOutConn = conns.binaryInTextOutConn;
    arrayArrayBinaryInBinaryOutConn = conns.arrayArrayBinaryInBinaryOutConn;
    arrayArrayTextInTextOutConn = conns.arrayArrayTextInTextOutConn;
    arrayArrayTextInBinaryOutConn = conns.arrayArrayTextInBinaryOutConn;
    arrayArrayBinaryInTextOutConn = conns.arrayArrayBinaryInTextOutConn;
  }

  @SuppressWarnings("unchecked")
  private SharedConnections createSharedConnections(Codec<A> codec, Class<A> type) {
    Class<List<A>> listClass = (Class<List<A>>) (Class<?>) List.class;
    Codec<List<A>> arrayCd = codec.inDim();
    @SuppressWarnings("unchecked")
    Class<List<List<A>>> listListClass = (Class<List<List<A>>>) (Class<?>) List.class;
    Codec<List<List<A>>> arrayArrayCd = arrayCd.inDim();

    // Each connection handles both the scalar and array codec.
    return new SharedConnections(
        r2dbcConnect(
            true,
            new BinaryInBinaryOutR2dbcCodec<>(codec, type),
            new BinaryInBinaryOutR2dbcCodec<>(arrayCd, listClass)),
        r2dbcConnect(
            false,
            new TextInTextOutR2dbcCodec<>(codec, type),
            new TextInTextOutR2dbcCodec<>(arrayCd, listClass)),
        r2dbcConnect(
            true,
            new TextInBinaryOutR2dbcCodec<>(codec, type),
            new TextInBinaryOutR2dbcCodec<>(arrayCd, listClass)),
        r2dbcConnect(
            false,
            new BinaryInTextOutR2dbcCodec<>(codec, type),
            new BinaryInTextOutR2dbcCodec<>(arrayCd, listClass)),
        r2dbcConnect(true, new BinaryInBinaryOutR2dbcCodec<>(arrayArrayCd, listListClass)),
        r2dbcConnect(false, new TextInTextOutR2dbcCodec<>(arrayArrayCd, listListClass)),
        r2dbcConnect(true, new TextInBinaryOutR2dbcCodec<>(arrayArrayCd, listListClass)),
        r2dbcConnect(false, new BinaryInTextOutR2dbcCodec<>(arrayArrayCd, listListClass)));
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------
  private Connection r2dbcConnect(
      boolean forceBinary, io.r2dbc.postgresql.codec.Codec<?>... r2dbcCodecs) {
    var builder =
        PostgresqlConnectionConfiguration.builder()
            .host(container.getHost())
            .port(container.getMappedPort(5432))
            .username(container.getUsername())
            .password(container.getPassword())
            .database(container.getDatabaseName())
            .codecRegistrar(
                (c, allocator, registry) -> {
                  for (var r2dbcCodec : r2dbcCodecs) {
                    registry.addFirst(r2dbcCodec);
                  }
                  return Mono.empty();
                });
    if (forceBinary) {
      builder.forceBinary(true);
    }
    return Mono.from(new PostgresqlConnectionFactory(builder.build()).create()).block();
  }

  private A roundtripViaR2dbc(Connection r2conn, A value) {
    return Flux.from(
            r2conn.createStatement("SELECT $1::" + codec.typeSig()).bind(0, value).execute())
        .flatMap(result -> result.map((row, meta) -> row.get(0, type)))
        .single()
        .block();
  }

  @SuppressWarnings("unchecked")
  private List<A> roundtripArrayViaR2dbc(Connection r2conn, List<A> value) {
    return (List<A>)
        Flux.from(
                r2conn
                    .createStatement("SELECT $1::" + arrayCodec.typeSig())
                    .bind(0, value)
                    .execute())
            .flatMap(result -> result.map((row, meta) -> row.get(0, List.class)))
            .single()
            .block();
  }

  @SuppressWarnings("unchecked")
  private List<List<A>> roundtripArrayArrayViaR2dbc(Connection r2conn, List<List<A>> value) {
    return (List<List<A>>)
        Flux.from(
                r2conn
                    .createStatement("SELECT $1::" + arrayArrayCodec.typeSig())
                    .bind(0, value)
                    .execute())
            .flatMap(result -> result.map((row, meta) -> row.get(0, List.class)))
            .single()
            .block();
  }

  @Provide
  Arbitrary<A> values() {
    return net.jqwik.api.Arbitraries.fromGeneratorWithSize(
        size -> r -> net.jqwik.api.Shrinkable.unshrinkable(codec.random(r, size)));
  }

  @Provide
  Arbitrary<List<A>> arrayValues() {
    return net.jqwik.api.Arbitraries.fromGeneratorWithSize(
        size -> r -> net.jqwik.api.Shrinkable.unshrinkable(arrayCodec.random(r, size)));
  }

  @Provide
  Arbitrary<List<List<A>>> arrayArrayValues() {
    return net.jqwik.api.Arbitraries.fromGeneratorWithSize(
        size -> r -> net.jqwik.api.Shrinkable.unshrinkable(arrayArrayCodec.random(r, size)));
  }

  // -----------------------------------------------------------------------
  // Scalar tests
  // -----------------------------------------------------------------------
  @Test
  void oidMatchesName() throws Exception {
    if (codec.scalarOid() == 0) {
      return;
    }
    try (var conn = jdbcPool.getConnection();
        var ps = conn.prepareStatement("SELECT oid::int FROM pg_type WHERE typname = ?")) {
      // TODO: Account for schema-qualified types names
      ps.setString(1, codec.name());
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          assertEquals(rs.getInt(1), codec.scalarOid(), "OID mismatch for " + codec.name());
        }
      }
    }
  }

  @Test
  void arrayOidMatchesName() throws Exception {
    if (arrayCodec.arrayOid() == 0) {
      return;
    }
    // Array types in pg_type are named "_<element_name>".
    try (var conn = jdbcPool.getConnection();
        var ps = conn.prepareStatement("SELECT oid::int FROM pg_type WHERE typname = ?")) {
      ps.setString(1, "_" + codec.name());
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          assertEquals(
              rs.getInt(1),
              arrayCodec.arrayOid(),
              "Array OID mismatch for " + arrayCodec.typeSig());
        }
      }
    }
  }

  @Property(tries = 100)
  void roundtripsInBinaryToBinaryViaR2dbc(@ForAll("values") A value) throws Exception {
    A decoded = roundtripViaR2dbc(binaryInBinaryOutConn, value);
    assertEquals(value, decoded, "decode mismatch for " + codec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void roundtripsInTextToTextViaR2dbc(@ForAll("values") A value) throws Exception {
    A decoded = roundtripViaR2dbc(textInTextOutConn, value);
    assertEquals(value, decoded, "decode mismatch for " + codec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void roundtripsInTextToBinaryViaR2dbc(@ForAll("values") A value) throws Exception {
    A decoded = roundtripViaR2dbc(textInBinaryOutConn, value);
    assertEquals(value, decoded, "decode mismatch for " + codec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void roundtripsInBinaryToTextViaR2dbc(@ForAll("values") A value) throws Exception {
    A decoded = roundtripViaR2dbc(binaryInTextOutConn, value);
    assertEquals(value, decoded, "decode mismatch for " + codec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void roundtripsInTextToTextViaPgjdbc(@ForAll("values") A value) throws Exception {
    try (var conn = jdbcPool.getConnection();
        var ps = conn.prepareStatement("SELECT ?::" + codec.typeSig())) {
      if (value != null) {
        PGobject obj = new PGobject();
        obj.setType(qualifiedCodecName(codec));
        obj.setValue(codec.encodeInTextToString(value));
        ps.setObject(1, obj);
      } else {
        ps.setNull(1, java.sql.Types.OTHER);
      }

      A decoded;
      try (ResultSet rs = ps.executeQuery()) {
        assertTrue(rs.next(), "Expected a result row");
        String text = rs.getString(1);
        if (text == null) {
          decoded = null;
        } else {
          var result = codec.decodeInText(text, 0);
          decoded = result.value;
        }
      }

      assertEquals(value, decoded, "decode mismatch for " + codec.typeSig() + " value=" + value);
    }
  }

  // -----------------------------------------------------------------------
  // Array tests
  // -----------------------------------------------------------------------

  @Property(tries = 100)
  void arrayRoundtripsInBinaryToBinaryViaR2dbc(@ForAll("arrayValues") List<A> value)
      throws Exception {
    List<A> decoded = roundtripArrayViaR2dbc(binaryInBinaryOutConn, value);
    assertEquals(value, decoded, "decode mismatch for " + arrayCodec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void arrayRoundtripsInTextToTextViaR2dbc(@ForAll("arrayValues") List<A> value) throws Exception {
    List<A> decoded = roundtripArrayViaR2dbc(textInTextOutConn, value);
    assertEquals(value, decoded, "decode mismatch for " + arrayCodec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void arrayRoundtripsInTextToBinaryViaR2dbc(@ForAll("arrayValues") List<A> value)
      throws Exception {
    List<A> decoded = roundtripArrayViaR2dbc(textInBinaryOutConn, value);
    assertEquals(value, decoded, "decode mismatch for " + arrayCodec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void arrayRoundtripsInBinaryToTextViaR2dbc(@ForAll("arrayValues") List<A> value)
      throws Exception {
    List<A> decoded = roundtripArrayViaR2dbc(binaryInTextOutConn, value);
    assertEquals(value, decoded, "decode mismatch for " + arrayCodec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void arrayRoundtripsInTextToTextViaPgjdbc(@ForAll("arrayValues") List<A> value) throws Exception {
    try (var conn = jdbcPool.getConnection();
        var ps = conn.prepareStatement("SELECT ?::" + arrayCodec.typeSig())) {
      if (value != null) {
        PGobject obj = new PGobject();
        obj.setType(qualifiedCodecName(arrayCodec));
        obj.setValue(arrayCodec.encodeInTextToString(value));
        ps.setObject(1, obj);
      } else {
        ps.setNull(1, java.sql.Types.OTHER);
      }

      List<A> decoded;
      try (ResultSet rs = ps.executeQuery()) {
        assertTrue(rs.next(), "Expected a result row");
        String text = rs.getString(1);
        if (text == null) {
          decoded = null;
        } else {
          decoded = arrayCodec.decodeInText(text, 0).value;
        }
      }

      assertEquals(
          value, decoded, "decode mismatch for " + arrayCodec.typeSig() + " value=" + value);
    }
  }

  @Property(tries = 100)
  void arrayArrayRoundtripsInBinaryToBinaryViaR2dbc(@ForAll("arrayArrayValues") List<List<A>> value)
      throws Exception {
    List<List<A>> decoded = roundtripArrayArrayViaR2dbc(arrayArrayBinaryInBinaryOutConn, value);
    assertEquals(
        value, decoded, "decode mismatch for " + arrayArrayCodec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void arrayArrayRoundtripsInTextToTextViaR2dbc(@ForAll("arrayArrayValues") List<List<A>> value)
      throws Exception {
    List<List<A>> decoded = roundtripArrayArrayViaR2dbc(arrayArrayTextInTextOutConn, value);
    assertEquals(
        value, decoded, "decode mismatch for " + arrayArrayCodec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void arrayArrayRoundtripsInTextToBinaryViaR2dbc(@ForAll("arrayArrayValues") List<List<A>> value)
      throws Exception {
    List<List<A>> decoded = roundtripArrayArrayViaR2dbc(arrayArrayTextInBinaryOutConn, value);
    assertEquals(
        value, decoded, "decode mismatch for " + arrayArrayCodec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void arrayArrayRoundtripsInBinaryToTextViaR2dbc(@ForAll("arrayArrayValues") List<List<A>> value)
      throws Exception {
    List<List<A>> decoded = roundtripArrayArrayViaR2dbc(arrayArrayBinaryInTextOutConn, value);
    assertEquals(
        value, decoded, "decode mismatch for " + arrayArrayCodec.typeSig() + " value=" + value);
  }

  @Property(tries = 100)
  void arrayArrayRoundtripsInTextToTextViaPgjdbc(@ForAll("arrayArrayValues") List<List<A>> value)
      throws Exception {
    try (var conn = jdbcPool.getConnection();
        var ps = conn.prepareStatement("SELECT ?::" + arrayArrayCodec.typeSig())) {
      if (value != null) {
        PGobject obj = new PGobject();
        obj.setType(pgjdbcCodecName(arrayArrayCodec));
        obj.setValue(arrayArrayCodec.encodeInTextToString(value));
        ps.setObject(1, obj);
      } else {
        ps.setNull(1, java.sql.Types.OTHER);
      }

      List<List<A>> decoded;
      try (ResultSet rs = ps.executeQuery()) {
        assertTrue(rs.next(), "Expected a result row");
        String text = rs.getString(1);
        if (text == null) {
          decoded = null;
        } else {
          decoded = arrayArrayCodec.decodeInText(text, 0).value;
        }
      }

      assertEquals(
          value, decoded, "decode mismatch for " + arrayArrayCodec.typeSig() + " value=" + value);
    }
  }

  private static String qualifiedCodecName(Codec codec) {
    StringBuilder sb = new StringBuilder();
    if (codec.schema() != null && !codec.schema().isEmpty()) {
      sb.append(codec.schema()).append(".");
    }
    sb.append(codec.name());
    for (int i = 0; i < codec.dimensions(); i++) {
      sb.append("[]");
    }
    return sb.toString();
  }

  private static String pgjdbcCodecName(Codec codec) {
    StringBuilder sb = new StringBuilder();
    if (codec.dimensions() > 0) {
      sb.append("_");
    }
    if (codec.schema() != null && !codec.schema().isEmpty()) {
      sb.append(codec.schema()).append(".");
    }
    sb.append(codec.name());
    return sb.toString();
  }
}
