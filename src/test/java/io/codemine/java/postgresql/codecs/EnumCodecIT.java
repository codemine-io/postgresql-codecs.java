package io.codemine.java.postgresql.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Shrinkable;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Integration tests for {@link EnumCodec} against a real PostgreSQL instance via pgjdbc.
 *
 * <p>The Docker container is shared via a {@code static} field (started once). The pgjdbc
 * connection is an instance field created in the constructor so that it is always available,
 * regardless of whether jqwik re-creates the test instance for a {@code @Property} method.
 */
class EnumCodecIT {

  // -----------------------------------------------------------------------
  // Test enum and codec
  // -----------------------------------------------------------------------
  enum Mood {
    HAPPY,
    SAD,
    NEUTRAL
  }

  private static final EnumCodec<Mood> MOOD_CODEC =
      new EnumCodec<>(
          "", "test_mood", Map.of(Mood.HAPPY, "happy", Mood.SAD, "sad", Mood.NEUTRAL, "neutral"));

  // -----------------------------------------------------------------------
  // Shared container (started once) + per-instance connections
  // -----------------------------------------------------------------------
  static final PostgreSQLContainer<?> CONTAINER;
  private static final AtomicBoolean DDL_DONE = new AtomicBoolean(false);

  static {
    CONTAINER = new PostgreSQLContainer<>("postgres:18");
    CONTAINER.start();
  }

  private final java.sql.Connection pgjdbcConn;

  EnumCodecIT() {
    try {
      var props = new java.util.Properties();
      props.setProperty("user", CONTAINER.getUsername());
      props.setProperty("password", CONTAINER.getPassword());
      props.setProperty("prepareThreshold", "0");
      pgjdbcConn = DriverManager.getConnection(CONTAINER.getJdbcUrl(), props);

      if (DDL_DONE.compareAndSet(false, true)) {
        try (var stmt = pgjdbcConn.createStatement()) {
          stmt.execute("CREATE TYPE test_mood AS ENUM ('happy', 'sad', 'neutral')");
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("EnumCodecIT setup failed", e);
    }
  }

  // -----------------------------------------------------------------------
  // Providers
  // -----------------------------------------------------------------------
  @Provide
  Arbitrary<Mood> moods() {
    return Arbitraries.fromGeneratorWithSize(
        size -> r -> Shrinkable.unshrinkable(MOOD_CODEC.random(r, size)));
  }

  // -----------------------------------------------------------------------
  // Tests
  // -----------------------------------------------------------------------
  @Test
  void oidMatchesName() throws Exception {
    try (var ps = pgjdbcConn.prepareStatement("SELECT oid::int FROM pg_type WHERE typname = ?")) {
      ps.setString(1, MOOD_CODEC.name());
      try (ResultSet rs = ps.executeQuery()) {
        assertTrue(rs.next(), "enum type test_mood not found in pg_type");
      }
    }
  }

  @Property(tries = 50)
  void roundtripsInTextToTextViaPgjdbc(@ForAll("moods") Mood value) throws Exception {
    try (var ps = pgjdbcConn.prepareStatement("SELECT ?::test_mood")) {
      PGobject obj = new PGobject();
      obj.setType("test_mood");
      StringBuilder sb = new StringBuilder();
      MOOD_CODEC.encodeInText(sb, value);
      obj.setValue(sb.toString());
      ps.setObject(1, obj);
      try (ResultSet rs = ps.executeQuery()) {
        assertTrue(rs.next());
        Mood decoded = MOOD_CODEC.decodeInText(rs.getString(1), 0).value;
        assertEquals(value, decoded);
      }
    }
  }
}
