package io.codemine.java.postgresql.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.postgresql.util.PGobject;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests that verify codecs against a real PostgreSQL instance via the
 * pgjdbc driver.
 *
 * <p>Each concrete subclass passes a {@link Codec} and its value type to the constructor. The base
 * class provides:
 *
 * <ul>
 *   <li>Text-protocol scalar and array roundtrip tests via pgjdbc.
 *   <li>Binary-output scalar and array roundtrip tests via pgjdbc, using PostgreSQL's type send
 *       function to retrieve the canonical binary representation and decoding it with the codec's
 *       binary decoder.
 *   <li>OID consistency checks that validate {@link Codec#scalarOid()} and {@link Codec#arrayOid()}
 *       against {@code pg_type}.
 * </ul>
 *
 * <p>A single shared container is started once for all subclasses. Connections are opened once per
 * concrete subclass and cached in {@link #connectionsByClass} so that jqwik's per-property instance
 * creation does not open fresh connections on each invocation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class CodecITBase<A> {

  static final PostgreSQLContainer<?> container;

  static {
    container =
        new PostgreSQLContainer<>("postgres:18").withCommand("postgres -c max_connections=300");
    container.start();
  }

  /**
   * Cache of shared pgjdbc connections keyed by concrete test class. Ensures that a connection is
   * opened exactly once per subclass regardless of how many instances jqwik creates.
   */
  private static final ConcurrentHashMap<Class<?>, java.sql.Connection> connectionsByClass =
      new ConcurrentHashMap<>();

  /**
   * Cache of PostgreSQL send-function names keyed by concrete test class. The send function is used
   * to obtain the canonical binary representation of a value directly from PostgreSQL. {@code
   * Optional.empty()} is stored when the type has no discoverable send function.
   */
  private static final ConcurrentHashMap<Class<?>, Optional<String>> sendFunctionsByClass =
      new ConcurrentHashMap<>();

  private final Codec<A> codec;
  private final Codec<List<A>> arrayCodec;

  /** Shared pgjdbc connection for this concrete test class. */
  private final java.sql.Connection pgjdbcConnection;

  /**
   * Name of the PostgreSQL send function for the scalar type (e.g. {@code "int4send"}), or {@code
   * null} if not available. Used by {@link #roundtripsInBinaryOutViaPgjdbc}.
   */
  private final String sendFunctionName;

  @SuppressWarnings("unchecked")
  protected CodecITBase(Codec<A> codec, Class<A> type) {
    this.codec = codec;
    this.arrayCodec = codec.inDim();

    pgjdbcConnection = connectionsByClass.computeIfAbsent(this.getClass(), cls -> openConnection());

    sendFunctionName =
        sendFunctionsByClass
            .computeIfAbsent(
                this.getClass(),
                cls -> Optional.ofNullable(lookupSendFunction(pgjdbcConnection, codec.name())))
            .orElse(null);
  }

  @AfterAll
  void closeConnections() throws Exception {
    java.sql.Connection conn = connectionsByClass.remove(this.getClass());
    sendFunctionsByClass.remove(this.getClass());
    if (conn != null) {
      conn.close();
    }
  }

  // -----------------------------------------------------------------------
  // Connection helpers
  // -----------------------------------------------------------------------

  private static java.sql.Connection openConnection() {
    try {
      var props = new java.util.Properties();
      props.setProperty("user", container.getUsername());
      props.setProperty("password", container.getPassword());
      // Disable server-side prepared-statement caching so that all result
      // columns remain in text format (avoids rs.getString() returning
      // "[B@…" for bytea columns after the binary-mode switch threshold).
      props.setProperty("prepareThreshold", "0");
      return DriverManager.getConnection(container.getJdbcUrl(), props);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to open pgjdbc connection", e);
    }
  }

  /**
   * Looks up the PostgreSQL send function name for a given type name by querying {@code pg_proc}
   * and {@code pg_type}. Returns {@code null} if the type is not found or has no send function.
   */
  private static String lookupSendFunction(java.sql.Connection conn, String typeName) {
    try (var ps =
        conn.prepareStatement(
            "SELECT p.proname FROM pg_proc p"
                + " JOIN pg_type t ON t.typsend = p.oid"
                + " WHERE t.typname = ?")) {
      ps.setString(1, typeName);
      try (var rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        }
      }
    } catch (SQLException e) {
      // Ignore — type may not exist yet (e.g. extension not installed)
    }
    return null;
  }

  // -----------------------------------------------------------------------
  // Providers
  // -----------------------------------------------------------------------

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

  // -----------------------------------------------------------------------
  // Catalog sanity checks
  // -----------------------------------------------------------------------

  @Test
  void oidMatchesName() throws Exception {
    if (codec.scalarOid() == 0) {
      return;
    }
    try (var ps =
        pgjdbcConnection.prepareStatement("SELECT oid::int FROM pg_type WHERE typname = ?")) {
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
    try (var ps =
        pgjdbcConnection.prepareStatement("SELECT oid::int FROM pg_type WHERE typname = ?")) {
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

  // -----------------------------------------------------------------------
  // Scalar tests — text protocol
  // -----------------------------------------------------------------------

  /**
   * Encodes the value as PostgreSQL text, sends it to the server via pgjdbc, reads the result back
   * as text, and decodes it. Verifies the full text encode→PostgreSQL→text decode roundtrip.
   */
  @Property(tries = 100)
  void roundtripsInTextToTextViaPgjdbc(@ForAll("values") A value) throws Exception {
    try (var ps = pgjdbcConnection.prepareStatement("SELECT ?::" + codec.typeSig())) {
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
  // Scalar tests — binary output via PostgreSQL send function
  // -----------------------------------------------------------------------

  /**
   * Sends the text-encoded value to PostgreSQL, retrieves the canonical binary representation via
   * the type's send function (e.g. {@code int4send}), and decodes it with the codec's binary
   * decoder. This verifies that the binary decoder correctly handles the bytes that PostgreSQL
   * itself produces.
   *
   * <p>Skipped automatically when the type's send function cannot be determined (e.g. for
   * user-defined types without a discoverable send function).
   */
  @Property(tries = 100)
  void roundtripsInBinaryOutViaPgjdbc(@ForAll("values") A value) throws Exception {
    if (sendFunctionName == null) {
      return;
    }

    PGobject obj = new PGobject();
    obj.setType(qualifiedCodecName(codec));
    obj.setValue(codec.encodeInTextToString(value));

    try (var ps =
        pgjdbcConnection.prepareStatement(
            "SELECT " + sendFunctionName + "(?::" + codec.typeSig() + ")")) {
      ps.setObject(1, obj);
      try (ResultSet rs = ps.executeQuery()) {
        assertTrue(rs.next(), "Expected a result row");
        byte[] bytes = rs.getBytes(1);
        assertNotNull(bytes, "send function returned null for " + codec.typeSig());
        A decoded = codec.decodeInBinary(ByteBuffer.wrap(bytes), bytes.length);
        assertEquals(value, decoded, "decode mismatch for " + codec.typeSig() + " value=" + value);
      }
    }
  }

  // -----------------------------------------------------------------------
  // Array tests — text protocol
  // -----------------------------------------------------------------------

  /**
   * Encodes the array value as PostgreSQL text, sends it to the server via pgjdbc, reads the result
   * back as text, and decodes it. Verifies the full text encode→PostgreSQL→text decode roundtrip
   * for arrays.
   */
  @Property(tries = 100)
  void arrayRoundtripsInTextToTextViaPgjdbc(@ForAll("arrayValues") List<A> value) throws Exception {
    try (var ps = pgjdbcConnection.prepareStatement("SELECT ?::" + arrayCodec.typeSig())) {
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

  // -----------------------------------------------------------------------
  // Array tests — binary output via array_send
  // -----------------------------------------------------------------------

  /**
   * Sends the text-encoded array to PostgreSQL and retrieves the canonical binary representation
   * via {@code array_send}. Decodes the result with the array codec's binary decoder. Verifies that
   * the binary array decoder correctly handles the bytes that PostgreSQL itself produces.
   */
  @Property(tries = 100)
  void arrayRoundtripsInBinaryOutViaPgjdbc(@ForAll("arrayValues") List<A> value) throws Exception {
    PGobject obj = new PGobject();
    obj.setType(qualifiedCodecName(arrayCodec));
    obj.setValue(arrayCodec.encodeInTextToString(value));

    try (var ps =
        pgjdbcConnection.prepareStatement("SELECT array_send(?::" + arrayCodec.typeSig() + ")")) {
      ps.setObject(1, obj);
      try (ResultSet rs = ps.executeQuery()) {
        assertTrue(rs.next(), "Expected a result row");
        byte[] bytes = rs.getBytes(1);
        assertNotNull(bytes, "array_send returned null for " + arrayCodec.typeSig());
        List<A> decoded = arrayCodec.decodeInBinary(ByteBuffer.wrap(bytes), bytes.length);
        assertEquals(
            value, decoded, "decode mismatch for " + arrayCodec.typeSig() + " value=" + value);
      }
    }
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  @SuppressWarnings("rawtypes")
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
}
