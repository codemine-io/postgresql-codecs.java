package io.pgenie.postgresqlCodecs.codecs;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.ResultSet;
import java.time.*;
import java.util.HexFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the binary wire format (encode/decodeBinary) of all
 * scalar codecs.
 *
 * <p>Each test:
 * <ol>
 *   <li>Encodes a Java value using {@link Codec#encode} and obtains a hex
 *       string of the resulting bytes.</li>
 *   <li>Queries PostgreSQL's built-in {@code *send()} function to obtain the
 *       canonical binary representation and converts that to a hex string.</li>
 *   <li>Asserts both hex strings are identical — this verifies our encoder
 *       produces exactly what PostgreSQL sends over the wire.</li>
 *   <li>Decodes the PostgreSQL-produced bytes back using
 *       {@link Codec#decodeBinary} and asserts the decoded value equals the
 *       original.</li>
 * </ol>
 */
public class BinaryCodecIT extends CodecITBase {

    // -----------------------------------------------------------------------
    // Helper: get binary bytes from PostgreSQL's *send() function
    // -----------------------------------------------------------------------

    /** Invokes {@code sendFn(value::pgType)} and returns the result as bytes. */
    private <A> byte[] pgSendBytes(Codec<A> codec, String pgType, String sendFn, A value)
            throws Exception {
        try (var conn = connect();
             var ps = conn.prepareStatement("SELECT " + sendFn + "(?::" + pgType + ")")) {
            codec.bind(ps, 1, value);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getBytes(1);
            }
        }
    }

    /** Hex-encodes a byte array using Java's HexFormat. */
    private static String hex(byte[] b) {
        return HexFormat.of().formatHex(b);
    }

    /** Wraps a byte array in a big-endian ByteBuffer ready for decoding. */
    private static ByteBuffer wrap(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Core helper: asserts encode matches PostgreSQL, and decode gives back
     * the original value using {@code Object.equals}.
     */
    private <A> void assertBinaryRoundTrip(Codec<A> codec, String pgType, String sendFn, A value)
            throws Exception {
        byte[] pgBytes = pgSendBytes(codec, pgType, sendFn, value);
        byte[] ourBytes = codec.encode(value);
        assertEquals(hex(pgBytes), hex(ourBytes),
                "encode mismatch for " + codec.name() + " value=" + value);

        A decoded = codec.decodeBinary(wrap(pgBytes), pgBytes.length);
        assertEquals(value, decoded,
                "decode mismatch for " + codec.name() + " value=" + value);
    }

    // -----------------------------------------------------------------------
    // OID metadata
    // -----------------------------------------------------------------------

    /** Verifies that codec.oid() matches the OID in pg_type. */
    private void assertOid(Codec<?> codec, String typname) throws Exception {
        if (codec.oid() == 0) return; // skip unknown OIDs
        try (var conn = connect();
             var ps = conn.prepareStatement("SELECT oid::int FROM pg_type WHERE typname = ?")) {
            ps.setString(1, typname);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int pgOid = rs.getInt(1);
                    assertEquals(pgOid, codec.oid(),
                            "OID mismatch for " + typname);
                }
            }
        }
    }

    @Test
    void oids() throws Exception {
        assertOid(Codec.BOOL, "bool");
        assertOid(Codec.INT2, "int2");
        assertOid(Codec.INT4, "int4");
        assertOid(Codec.INT8, "int8");
        assertOid(Codec.FLOAT4, "float4");
        assertOid(Codec.FLOAT8, "float8");
        assertOid(Codec.NUMERIC, "numeric");
        assertOid(Codec.TEXT, "text");
        assertOid(Codec.BYTEA, "bytea");
        assertOid(Codec.DATE, "date");
        assertOid(Codec.TIME, "time");
        assertOid(Codec.TIMETZ, "timetz");
        assertOid(Codec.TIMESTAMP, "timestamp");
        assertOid(Codec.TIMESTAMPTZ, "timestamptz");
        assertOid(Codec.UUID, "uuid");
        assertOid(Codec.JSON, "json");
        assertOid(Codec.JSONB, "jsonb");
        assertOid(Codec.OID, "oid");
        assertOid(Codec.INET, "inet");
        assertOid(Codec.CIDR, "cidr");
        assertOid(Codec.MACADDR, "macaddr");
        assertOid(Codec.MACADDR8, "macaddr8");
        assertOid(Codec.POINT, "point");
        assertOid(Codec.LINE, "line");
        assertOid(Codec.LSEG, "lseg");
        assertOid(Codec.BOX, "box");
        assertOid(Codec.CIRCLE, "circle");
        assertOid(Codec.BIT, "bit");
        assertOid(Codec.VARBIT, "varbit");
    }

    // -----------------------------------------------------------------------
    // Bool
    // -----------------------------------------------------------------------

    @Test
    void boolTrue() throws Exception {
        assertBinaryRoundTrip(Codec.BOOL, "bool", "boolsend", true);
    }

    @Test
    void boolFalse() throws Exception {
        assertBinaryRoundTrip(Codec.BOOL, "bool", "boolsend", false);
    }

    // -----------------------------------------------------------------------
    // Integers
    // -----------------------------------------------------------------------

    @Test
    void int2Values() throws Exception {
        assertBinaryRoundTrip(Codec.INT2, "int2", "int2send", (short) 0);
        assertBinaryRoundTrip(Codec.INT2, "int2", "int2send", (short) 32767);
        assertBinaryRoundTrip(Codec.INT2, "int2", "int2send", (short) -32768);
    }

    @Test
    void int4Values() throws Exception {
        assertBinaryRoundTrip(Codec.INT4, "int4", "int4send", 0);
        assertBinaryRoundTrip(Codec.INT4, "int4", "int4send", 42);
        assertBinaryRoundTrip(Codec.INT4, "int4", "int4send", -1);
        assertBinaryRoundTrip(Codec.INT4, "int4", "int4send", Integer.MAX_VALUE);
        assertBinaryRoundTrip(Codec.INT4, "int4", "int4send", Integer.MIN_VALUE);
    }

    @Test
    void int8Values() throws Exception {
        assertBinaryRoundTrip(Codec.INT8, "int8", "int8send", 0L);
        assertBinaryRoundTrip(Codec.INT8, "int8", "int8send", 9876543210L);
        assertBinaryRoundTrip(Codec.INT8, "int8", "int8send", -9876543210L);
        assertBinaryRoundTrip(Codec.INT8, "int8", "int8send", Long.MAX_VALUE);
    }

    // -----------------------------------------------------------------------
    // Floats
    // -----------------------------------------------------------------------

    @Test
    void float4Values() throws Exception {
        assertBinaryRoundTrip(Codec.FLOAT4, "float4", "float4send", 0.0f);
        assertBinaryRoundTrip(Codec.FLOAT4, "float4", "float4send", 3.14f);
        assertBinaryRoundTrip(Codec.FLOAT4, "float4", "float4send", -1.5f);
    }

    @Test
    void float8Values() throws Exception {
        assertBinaryRoundTrip(Codec.FLOAT8, "float8", "float8send", 0.0);
        assertBinaryRoundTrip(Codec.FLOAT8, "float8", "float8send", Math.PI);
        assertBinaryRoundTrip(Codec.FLOAT8, "float8", "float8send", -1.23456789e10);
    }

    // -----------------------------------------------------------------------
    // Numeric
    // -----------------------------------------------------------------------

    @Test
    void numericValues() throws Exception {
        assertBinaryRoundTrip(Codec.NUMERIC, "numeric", "numeric_send", BigDecimal.ZERO);
        assertBinaryRoundTrip(Codec.NUMERIC, "numeric", "numeric_send", new BigDecimal("1"));
        assertBinaryRoundTrip(Codec.NUMERIC, "numeric", "numeric_send", new BigDecimal("123456.789"));
        assertBinaryRoundTrip(Codec.NUMERIC, "numeric", "numeric_send", new BigDecimal("-0.00001"));
        assertBinaryRoundTrip(Codec.NUMERIC, "numeric", "numeric_send", new BigDecimal("99999999999.99"));
        assertBinaryRoundTrip(Codec.NUMERIC, "numeric", "numeric_send", new BigDecimal("0.1"));
    }

    // -----------------------------------------------------------------------
    // Text
    // -----------------------------------------------------------------------

    @Test
    void textValues() throws Exception {
        assertBinaryRoundTrip(Codec.TEXT, "text", "textsend", "");
        assertBinaryRoundTrip(Codec.TEXT, "text", "textsend", "hello");
        assertBinaryRoundTrip(Codec.TEXT, "text", "textsend", "Unicode: \u00e9\u4e2d\u6587");
        assertBinaryRoundTrip(Codec.TEXT, "text", "textsend", "line1\nline2");
    }

    // -----------------------------------------------------------------------
    // Bytea
    // -----------------------------------------------------------------------

    @Test
    void byteaValues() throws Exception {
        // bytea encode test: compare our bytes with PostgreSQL's byteasend output
        byte[] value = new byte[]{0x00, 0x01, (byte) 0xFF, (byte) 0xAB, 0x42};
        byte[] pgBytes = pgSendBytes(Codec.BYTEA, "bytea", "byteasend", value);
        byte[] ourBytes = Codec.BYTEA.encode(value);
        assertArrayEquals(pgBytes, ourBytes);

        // decode
        byte[] decoded = Codec.BYTEA.decodeBinary(wrap(pgBytes), pgBytes.length);
        assertArrayEquals(value, decoded);
    }

    @Test
    void byteaEmpty() throws Exception {
        byte[] value = new byte[0];
        byte[] pgBytes = pgSendBytes(Codec.BYTEA, "bytea", "byteasend", value);
        assertArrayEquals(pgBytes, Codec.BYTEA.encode(value));
        assertArrayEquals(value, Codec.BYTEA.decodeBinary(wrap(pgBytes), pgBytes.length));
    }

    // -----------------------------------------------------------------------
    // Date / Time
    // -----------------------------------------------------------------------

    @Test
    void dateValues() throws Exception {
        assertBinaryRoundTrip(Codec.DATE, "date", "date_send", LocalDate.of(2000, 1, 1));
        assertBinaryRoundTrip(Codec.DATE, "date", "date_send", LocalDate.of(1970, 1, 1));
        assertBinaryRoundTrip(Codec.DATE, "date", "date_send", LocalDate.of(2024, 12, 31));
        assertBinaryRoundTrip(Codec.DATE, "date", "date_send", LocalDate.of(1900, 6, 15));
    }

    @Test
    void timeValues() throws Exception {
        assertBinaryRoundTrip(Codec.TIME, "time", "time_send", LocalTime.MIDNIGHT);
        assertBinaryRoundTrip(Codec.TIME, "time", "time_send", LocalTime.NOON);
        assertBinaryRoundTrip(Codec.TIME, "time", "time_send", LocalTime.of(13, 45, 30, 123456000));
        assertBinaryRoundTrip(Codec.TIME, "time", "time_send", LocalTime.of(23, 59, 59, 999999000));
    }

    @Test
    void timetzValues() throws Exception {
        assertBinaryRoundTrip(Codec.TIMETZ, "timetz", "timetz_send",
                LocalTime.of(12, 0, 0).atOffset(ZoneOffset.UTC));
        assertBinaryRoundTrip(Codec.TIMETZ, "timetz", "timetz_send",
                LocalTime.of(9, 30, 0).atOffset(ZoneOffset.ofHours(5)));
        assertBinaryRoundTrip(Codec.TIMETZ, "timetz", "timetz_send",
                LocalTime.of(18, 0, 0).atOffset(ZoneOffset.ofHours(-8)));
    }

    @Test
    void timestampValues() throws Exception {
        assertBinaryRoundTrip(Codec.TIMESTAMP, "timestamp", "timestamp_send",
                LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        assertBinaryRoundTrip(Codec.TIMESTAMP, "timestamp", "timestamp_send",
                LocalDateTime.of(2024, 6, 15, 12, 30, 45, 123456000));
        assertBinaryRoundTrip(Codec.TIMESTAMP, "timestamp", "timestamp_send",
                LocalDateTime.of(1970, 1, 1, 0, 0, 0));
    }

    @Test
    void timestamptzValues() throws Exception {
        assertBinaryRoundTrip(Codec.TIMESTAMPTZ, "timestamptz", "timestamptz_send",
                OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        // timestamptz stores only the UTC instant; decode always returns ZoneOffset.UTC
        assertBinaryRoundTrip(Codec.TIMESTAMPTZ, "timestamptz", "timestamptz_send",
                OffsetDateTime.of(2024, 6, 15, 9, 30, 45, 123456000, ZoneOffset.UTC));
    }

    // -----------------------------------------------------------------------
    // UUID
    // -----------------------------------------------------------------------

    @Test
    void uuidValues() throws Exception {
        assertBinaryRoundTrip(Codec.UUID, "uuid", "uuid_send",
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        assertBinaryRoundTrip(Codec.UUID, "uuid", "uuid_send", UUID.randomUUID());
    }

    // -----------------------------------------------------------------------
    // Network types
    // -----------------------------------------------------------------------

    @Test
    void inetIpv4() throws Exception {
        assertBinaryRoundTrip(Codec.INET, "inet", "inet_send", "192.168.1.1/32");
        assertBinaryRoundTrip(Codec.INET, "inet", "inet_send", "10.0.0.0/8");
        assertBinaryRoundTrip(Codec.INET, "inet", "inet_send", "0.0.0.0/0");
    }

    @Test
    void inetIpv6() throws Exception {
        assertBinaryRoundTrip(Codec.INET, "inet", "inet_send", "::1/128");
        assertBinaryRoundTrip(Codec.INET, "inet", "inet_send", "2001:db8::/32");
    }

    @Test
    void macaddr() throws Exception {
        assertBinaryRoundTrip(Codec.MACADDR, "macaddr", "macaddr_send", "08:00:2b:01:02:03");
        assertBinaryRoundTrip(Codec.MACADDR, "macaddr", "macaddr_send", "ff:ff:ff:ff:ff:ff");
    }

    @Test
    void macaddr8() throws Exception {
        assertBinaryRoundTrip(Codec.MACADDR8, "macaddr8", "macaddr8_send", "08:00:2b:ff:fe:01:02:03");
    }

    // -----------------------------------------------------------------------
    // Geometric types
    // -----------------------------------------------------------------------

    @Test
    void point() throws Exception {
        assertBinaryRoundTrip(Codec.POINT, "point", "point_send",
                new org.postgresql.geometric.PGpoint(1.5, -2.5));
        assertBinaryRoundTrip(Codec.POINT, "point", "point_send",
                new org.postgresql.geometric.PGpoint(0, 0));
    }

    @Test
    void lseg() throws Exception {
        assertBinaryRoundTrip(Codec.LSEG, "lseg", "lseg_send",
                new org.postgresql.geometric.PGlseg(0, 0, 1, 1));
    }

    @Test
    void box() throws Exception {
        assertBinaryRoundTrip(Codec.BOX, "box", "box_send",
                new org.postgresql.geometric.PGbox(2, 2, 0, 0));
    }

    @Test
    void circle() throws Exception {
        assertBinaryRoundTrip(Codec.CIRCLE, "circle", "circle_send",
                new org.postgresql.geometric.PGcircle(
                        new org.postgresql.geometric.PGpoint(1.0, 2.0), 5.0));
    }

    @Test
    void path() throws Exception {
        org.postgresql.geometric.PGpath openPath = new org.postgresql.geometric.PGpath(
                new org.postgresql.geometric.PGpoint[]{
                        new org.postgresql.geometric.PGpoint(0, 0),
                        new org.postgresql.geometric.PGpoint(1, 0),
                        new org.postgresql.geometric.PGpoint(1, 1)
                }, true);
        byte[] pgBytes = pgSendBytes(Codec.PATH, "path", "path_send", openPath);
        byte[] ourBytes = Codec.PATH.encode(openPath);
        assertEquals(hex(pgBytes), hex(ourBytes));
        // Decode and check point count
        org.postgresql.geometric.PGpath decoded =
                Codec.PATH.decodeBinary(wrap(pgBytes), pgBytes.length);
        assertEquals(3, decoded.points.length);
        assertTrue(decoded.open);
    }

    @Test
    void polygon() throws Exception {
        org.postgresql.geometric.PGpolygon poly = new org.postgresql.geometric.PGpolygon(
                new org.postgresql.geometric.PGpoint[]{
                        new org.postgresql.geometric.PGpoint(0, 0),
                        new org.postgresql.geometric.PGpoint(1, 0),
                        new org.postgresql.geometric.PGpoint(0.5, 1)
                });
        byte[] pgBytes = pgSendBytes(Codec.POLYGON, "polygon", "poly_send", poly);
        byte[] ourBytes = Codec.POLYGON.encode(poly);
        assertEquals(hex(pgBytes), hex(ourBytes));
        org.postgresql.geometric.PGpolygon decoded =
                Codec.POLYGON.decodeBinary(wrap(pgBytes), pgBytes.length);
        assertEquals(3, decoded.points.length);
    }

    // -----------------------------------------------------------------------
    // Bit strings
    // -----------------------------------------------------------------------

    @Test
    void bit() throws Exception {
        // bit(6) — must specify exact length in the cast
        var bitCodec = Codec.BIT;
        String value = "101011";
        byte[] pgBytes = pgSendBytes(bitCodec, "bit(6)", "bit_send", value);
        byte[] ourBytes = bitCodec.encode(value);
        assertEquals(hex(pgBytes), hex(ourBytes));
        assertEquals(value, bitCodec.decodeBinary(wrap(pgBytes), pgBytes.length));
    }

    @Test
    void varbit() throws Exception {
        String value = "10110";
        byte[] pgBytes = pgSendBytes(Codec.VARBIT, "varbit", "varbit_send", value);
        byte[] ourBytes = Codec.VARBIT.encode(value);
        assertEquals(hex(pgBytes), hex(ourBytes));
        assertEquals(value, Codec.VARBIT.decodeBinary(wrap(pgBytes), pgBytes.length));
    }

    // -----------------------------------------------------------------------
    // Arrays — pure Java round-trip (PostgreSQL result parsed via text)
    // -----------------------------------------------------------------------

    @Test
    void arrayInt4BinaryRoundTrip() throws Exception {
        var codec = new ArrayCodec<>("_int4", Codec.INT4);
        var list = java.util.List.of(1, 2, 3, -100, Integer.MAX_VALUE);
        byte[] encoded = codec.encode(list);
        var decoded = codec.decodeBinary(wrap(encoded), encoded.length);
        assertEquals(list, decoded);
    }

    @Test
    void arrayTextBinaryRoundTrip() throws Exception {
        var codec = new ArrayCodec<>("_text", Codec.TEXT);
        var list = java.util.List.of("hello", "world", "Unicode: \u4e2d\u6587");
        byte[] encoded = codec.encode(list);
        var decoded = codec.decodeBinary(wrap(encoded), encoded.length);
        assertEquals(list, decoded);
    }

    @Test
    void arrayWithNullBinaryRoundTrip() throws Exception {
        var codec = new ArrayCodec<>("_int4", Codec.INT4);
        var list = new java.util.ArrayList<Integer>();
        list.add(1);
        list.add(null);
        list.add(3);
        byte[] encoded = codec.encode(list);
        var decoded = codec.decodeBinary(wrap(encoded), encoded.length);
        assertEquals(list, decoded);
    }

    @Test
    void arrayEmptyBinaryRoundTrip() throws Exception {
        var codec = new ArrayCodec<>("_int4", Codec.INT4);
        var list = java.util.List.<Integer>of();
        byte[] encoded = codec.encode(list);
        var decoded = codec.decodeBinary(wrap(encoded), encoded.length);
        assertEquals(list, decoded);
    }

    // -----------------------------------------------------------------------
    // Composite — pure Java round-trip
    // -----------------------------------------------------------------------

    @Test
    void compositeBinaryRoundTrip() throws Exception {
        record Point(int x, int y) {}
        var codec = new CompositeCodec<>("public", "mypoint",
                (Integer x) -> (Integer y) -> new Point(x, y),
                new CompositeCodec.Field<>("x", Point::x, Codec.INT4),
                new CompositeCodec.Field<>("y", Point::y, Codec.INT4));

        var value = new Point(7, -3);
        byte[] encoded = codec.encode(value);
        var decoded = codec.decodeBinary(wrap(encoded), encoded.length);
        assertEquals(value, decoded);
    }

    @Test
    void compositeWithNullFieldBinaryRoundTrip() throws Exception {
        record Named(String name, Integer count) {}
        var codec = new CompositeCodec<>("public", "named",
                (String name) -> (Integer count) -> new Named(name, count),
                new CompositeCodec.Field<>("name", Named::name, Codec.TEXT),
                new CompositeCodec.Field<>("count", Named::count, Codec.INT4));

        var value = new Named("alice", null);
        byte[] encoded = codec.encode(value);
        var decoded = codec.decodeBinary(wrap(encoded), encoded.length);
        assertEquals(value, decoded);
    }

}
