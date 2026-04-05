package io.codemine.java.postgresql.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.codemine.java.postgresql.codecs.CompositeCodecTest.AnnotatedSegment;
import io.codemine.java.postgresql.codecs.CompositeCodecTest.Point;
import io.codemine.java.postgresql.codecs.CompositeCodecTest.Segment;
import io.codemine.java.postgresql.codecs.CompositeCodecTest.TaggedData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.List;
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
 * Integration tests for {@link CompositeCodec}, exercising four composite scenarios against a real
 * PostgreSQL instance via pgjdbc.
 *
 * <p>The Docker container is shared via a {@code static} field. The pgjdbc connection is a
 * per-instance field (constructor-initialized) so that it is always available regardless of whether
 * jqwik re-creates the test instance for a {@code @Property} method. The DDL is guarded by an
 * {@code AtomicBoolean} so composite types are created only once.
 */
class CompositeCodecIT {

  // -----------------------------------------------------------------------
  // Shared container (started once)
  // -----------------------------------------------------------------------
  static final PostgreSQLContainer<?> CONTAINER;
  private static final AtomicBoolean DDL_DONE = new AtomicBoolean(false);

  static {
    CONTAINER = new PostgreSQLContainer<>("postgres:18");
    CONTAINER.start();
  }

  // -----------------------------------------------------------------------
  // Per-instance connection
  // -----------------------------------------------------------------------
  private final java.sql.Connection pgjdbcConn;

  CompositeCodecIT() {
    try {
      var props = new java.util.Properties();
      props.setProperty("user", CONTAINER.getUsername());
      props.setProperty("password", CONTAINER.getPassword());
      props.setProperty("prepareThreshold", "0");
      pgjdbcConn = DriverManager.getConnection(CONTAINER.getJdbcUrl(), props);

      // Create types in dependency order (leaf types first), only once.
      if (DDL_DONE.compareAndSet(false, true)) {
        try (var stmt = pgjdbcConn.createStatement()) {
          stmt.execute("CREATE TYPE test_pt     AS (x int4, y int4)");
          stmt.execute("CREATE TYPE test_seg    AS (start_pt test_pt, end_pt test_pt)");
          stmt.execute("CREATE TYPE test_tagged AS (tag text, items text[])");
          stmt.execute("CREATE TYPE test_ann_seg AS (label text, seg test_seg, tags text[])");
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("CompositeCodecIT setup failed", e);
    }
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------
  private <A> A roundtripViaPgjdbc(Codec<A> codec, A value) throws Exception {
    try (var ps = pgjdbcConn.prepareStatement("SELECT ?::" + codec.typeSig())) {
      PGobject obj = new PGobject();
      obj.setType(codec.typeSig());
      StringBuilder sb = new StringBuilder();
      codec.encodeInText(sb, value);
      obj.setValue(sb.toString());
      ps.setObject(1, obj);
      try (ResultSet rs = ps.executeQuery()) {
        assertTrue(rs.next(), "Expected a result row");
        return codec.decodeInText(rs.getString(1), 0).value;
      }
    }
  }

  // -----------------------------------------------------------------------
  // Catalog sanity check
  // -----------------------------------------------------------------------
  @Test
  void compositeTypesExistInCatalog() throws Exception {
    for (String typeName : List.of("test_pt", "test_seg", "test_tagged", "test_ann_seg")) {
      try (var ps = pgjdbcConn.prepareStatement("SELECT oid FROM pg_type WHERE typname = ?")) {
        ps.setString(1, typeName);
        try (ResultSet rs = ps.executeQuery()) {
          assertTrue(rs.next(), typeName + " not found in pg_type");
        }
      }
    }
  }

  // -----------------------------------------------------------------------
  // Providers
  // -----------------------------------------------------------------------
  @Provide("points")
  Arbitrary<Point> points() {
    return Arbitraries.fromGeneratorWithSize(
        size -> r -> Shrinkable.unshrinkable(CompositeCodecTest.POINT_CODEC.random(r, size)));
  }

  @Provide("segments")
  Arbitrary<Segment> segments() {
    return Arbitraries.fromGeneratorWithSize(
        size -> r -> Shrinkable.unshrinkable(CompositeCodecTest.SEGMENT_CODEC.random(r, size)));
  }

  @Provide("taggedData")
  Arbitrary<TaggedData> taggedData() {
    return Arbitraries.fromGeneratorWithSize(
        size -> r -> Shrinkable.unshrinkable(CompositeCodecTest.TAGGED_DATA_CODEC.random(r, size)));
  }

  @Provide("annotatedSegments")
  Arbitrary<AnnotatedSegment> annotatedSegments() {
    return Arbitraries.fromGeneratorWithSize(
        size -> r -> Shrinkable.unshrinkable(CompositeCodecTest.ANNOTATED_CODEC.random(r, size)));
  }

  // -----------------------------------------------------------------------
  // Simple 2-field composite: (x int4, y int4)
  // -----------------------------------------------------------------------
  @Property(tries = 50)
  void point_pgjdbc(@ForAll("points") Point v) throws Exception {
    assertEquals(v, roundtripViaPgjdbc(CompositeCodecTest.POINT_CODEC, v));
  }

  // -----------------------------------------------------------------------
  // Nested composite: (start test_pt, end test_pt)
  // -----------------------------------------------------------------------
  @Property(tries = 50)
  void segment_pgjdbc(@ForAll("segments") Segment v) throws Exception {
    assertEquals(v, roundtripViaPgjdbc(CompositeCodecTest.SEGMENT_CODEC, v));
  }

  // -----------------------------------------------------------------------
  // Composite with array field: (tag text, items text[])
  // -----------------------------------------------------------------------
  @Property(tries = 50)
  void taggedData_pgjdbc(@ForAll("taggedData") TaggedData v) throws Exception {
    assertEquals(v, roundtripViaPgjdbc(CompositeCodecTest.TAGGED_DATA_CODEC, v));
  }

  // -----------------------------------------------------------------------
  // Composite with nested composite + array: (label text, seg test_seg, tags text[])
  // -----------------------------------------------------------------------
  @Property(tries = 50)
  void annotatedSegment_pgjdbc(@ForAll("annotatedSegments") AnnotatedSegment v) throws Exception {
    assertEquals(v, roundtripViaPgjdbc(CompositeCodecTest.ANNOTATED_CODEC, v));
  }
}
