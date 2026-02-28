package io.pgenie.postgresqlCodecs.codecs;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.postgresql.geometric.PGbox;
import org.postgresql.geometric.PGcircle;
import org.postgresql.geometric.PGline;
import org.postgresql.geometric.PGlseg;
import org.postgresql.geometric.PGpath;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;

/**
 * Arbitrary value generators for each PostgreSQL codec type.
 *
 * <p>Each static method returns a {@link Stream} of {@link Arguments} suitable
 * for use with JUnit 5's {@code @MethodSource}, producing {@value #COUNT}
 * randomly-generated values that cover the full valid range of the
 * corresponding PostgreSQL type.
 *
 * <p>Analogous to the {@code Arbitrary} instances in the Haskell
 * <a href="https://github.com/nikita-volkov/postgresql-types">postgresql-types</a>
 * library.
 */
public final class Generators {

    /** Number of random samples per property test (mirrors QuickCheck's default). */
    static final int COUNT = 100;

    // PostgreSQL date range: 4713 BC (= proleptic year -4712) to 5874897 AD.
    private static final long DATE_MIN_EPOCH_DAY = LocalDate.of(-4712, 1, 1).toEpochDay();
    private static final long DATE_MAX_EPOCH_DAY = LocalDate.of(5874897, 12, 31).toEpochDay();
    // Lower bound for AD-only generators (avoids JDBC binding limitations for BC dates).
    private static final long DATE_AD_EPOCH_DAY = LocalDate.of(1, 1, 1).toEpochDay();

    // PostgreSQL timestamp range: 4713 BC to 294276 AD (stored as microseconds since PG epoch).
    // PG epoch = 2000-01-01 00:00:00 UTC.
    // Min micros offset from PG epoch: -210866803200000000 (4713 BC Jan 1)
    // Max micros offset from PG epoch: 9214646400000000 (294276 AD Dec 31)
    // We use the same epoch-day bounds for date part and full micro precision for time.
    private static final long TIMESTAMP_MIN_EPOCH_DAY = DATE_MIN_EPOCH_DAY;
    private static final long TIMESTAMP_MAX_EPOCH_DAY = LocalDate.of(294276, 12, 31).toEpochDay();

    private Generators() {
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static SourceOfRandomness rng() {
        return new SourceOfRandomness(new Random());
    }

    // -----------------------------------------------------------------------
    // Boolean
    // -----------------------------------------------------------------------

    /** Arbitrary {@code bool} values. */
    public static Stream<Arguments> booleans() {
        var r = rng();
        return Stream.generate(() -> Arguments.of(r.nextBoolean())).limit(COUNT);
    }

    // -----------------------------------------------------------------------
    // Integer types
    // -----------------------------------------------------------------------

    /** Arbitrary {@code int2} values covering the full [-32768, 32767] range. */
    public static Stream<Arguments> int2s() {
        var r = rng();
        return Stream.generate(() -> Arguments.of(r.nextShort(Short.MIN_VALUE, Short.MAX_VALUE))).limit(COUNT);
    }

    /** Arbitrary {@code int4} values covering the full 32-bit signed integer range. */
    public static Stream<Arguments> int4s() {
        // Use Random.nextInt() to uniformly cover the full range including MIN/MAX.
        var rnd = new Random();
        return Stream.generate(() -> Arguments.of(rnd.nextInt())).limit(COUNT);
    }

    /** Arbitrary {@code int8} values covering the full 64-bit signed integer range. */
    public static Stream<Arguments> int8s() {
        var rnd = new Random();
        return Stream.generate(() -> Arguments.of(rnd.nextLong())).limit(COUNT);
    }

    // -----------------------------------------------------------------------
    // Floating-point types (finite values only for equality-based round-trips)
    // -----------------------------------------------------------------------

    /**
     * Arbitrary finite {@code float4} values.
     *
     * <p>NaN and ±Infinity are excluded because {@code NaN != NaN} under
     * {@link Float#equals}; the existing fixed tests cover those special
     * values explicitly.
     */
    public static Stream<Arguments> float4s() {
        var rnd = new Random();
        return Stream.generate(() -> {
            float v;
            do {
                v = Float.intBitsToFloat(rnd.nextInt());
            } while (!Float.isFinite(v));
            return Arguments.of(v);
        }).limit(COUNT);
    }

    /**
     * Arbitrary finite {@code float8} values.
     *
     * <p>NaN and ±Infinity are excluded because {@code NaN != NaN} under
     * {@link Double#equals}; the existing fixed tests cover those special
     * values explicitly.
     */
    public static Stream<Arguments> float8s() {
        var rnd = new Random();
        return Stream.generate(() -> {
            double v;
            do {
                v = Double.longBitsToDouble(rnd.nextLong());
            } while (!Double.isFinite(v));
            return Arguments.of(v);
        }).limit(COUNT);
    }

    // -----------------------------------------------------------------------
    // Numeric
    // -----------------------------------------------------------------------

    /**
     * Arbitrary {@code numeric} values with varying precision and scale.
     *
     * <p>Covers: zero, small integers, large integers, fractional values,
     * and negative values.
     */
    public static Stream<Arguments> numerics() {
        var r = rng();
        return Stream.generate(() -> {
            boolean negative = r.nextBoolean();
            int intDigits = r.nextInt(0, 10);
            int fracDigits = r.nextInt(0, 7);
            StringBuilder sb = new StringBuilder();
            if (negative) sb.append('-');
            for (int i = 0; i < intDigits; i++) {
                sb.append((char) ('0' + r.nextInt(0, 9)));
            }
            if (intDigits == 0) sb.append('0');
            if (fracDigits > 0) {
                sb.append('.');
                for (int i = 0; i < fracDigits; i++) {
                    sb.append((char) ('0' + r.nextInt(0, 9)));
                }
                // Ensure at least one non-zero fractional digit to preserve scale
                if (sb.toString().matches("-?0+\\.0+")) {
                    sb.setCharAt(sb.length() - 1, '1');
                }
            }
            return Arguments.of(new BigDecimal(sb.toString()));
        }).limit(COUNT);
    }

    // -----------------------------------------------------------------------
    // Text types (strings without NUL characters)
    // -----------------------------------------------------------------------

    /**
     * Arbitrary {@code text} values — Unicode strings that exclude the NUL
     * character ({@code '\0'}), which PostgreSQL does not allow in text.
     */
    public static Stream<Arguments> texts() {
        var r = rng();
        return Stream.generate(() -> Arguments.of(arbitraryText(r, 50))).limit(COUNT);
    }

    /** Arbitrary {@code varchar} values (same constraints as {@code text}). */
    public static Stream<Arguments> varchars() {
        var r = rng();
        return Stream.generate(() -> Arguments.of(arbitraryText(r, 50))).limit(COUNT);
    }

    /**
     * Arbitrary {@code char(1)} values — single non-NUL characters.
     *
     * <p>PostgreSQL {@code char} is blank-padded; a single-character string
     * is the natural unit here.
     */
    public static Stream<Arguments> chars() {
        var r = rng();
        return Stream.generate(() -> {
            char c;
            do {
                // Limit to printable ASCII to avoid blank-padding round-trip surprises
                // with multi-byte Unicode in a fixed-length char(1) context.
                c = r.nextChar('!', '~');
            } while (c == '\0');
            return Arguments.of(String.valueOf(c));
        }).limit(COUNT);
    }

    private static String arbitraryText(SourceOfRandomness r, int maxLen) {
        int len = r.nextInt(0, maxLen);
        var sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c;
            do {
                // Generate printable Unicode BMP characters excluding surrogates and NUL.
                c = r.nextChar('\u0001', '\uD7FF');
            } while (c == '\0');
            sb.append(c);
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Bytea
    // -----------------------------------------------------------------------

    /** Arbitrary {@code bytea} values (random byte arrays). */
    public static Stream<Arguments> byteas() {
        var r = rng();
        return Stream.generate(() -> {
            int len = r.nextInt(0, 100);
            byte[] bytes = new byte[len];
            for (int i = 0; i < len; i++) {
                bytes[i] = r.nextByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
            }
            return Arguments.of((Object) bytes);
        }).limit(COUNT);
    }

    // -----------------------------------------------------------------------
    // Date / time types
    // -----------------------------------------------------------------------

    /**
     * Arbitrary {@code date} values spanning the full PostgreSQL range (4713 BC to 5874897 AD).
     *
     * <p>Use this generator for binary round-trip tests.  For text round-trip
     * tests use {@link #datesAD()} which restricts to AD dates to avoid JDBC
     * binding limitations for BC dates.
     */
    public static Stream<Arguments> dates() {
        var r = rng();
        return Stream.generate(() -> {
            long day = r.nextLong(DATE_MIN_EPOCH_DAY, DATE_MAX_EPOCH_DAY);
            return Arguments.of(LocalDate.ofEpochDay(day));
        }).limit(COUNT);
    }

    /**
     * Arbitrary AD-only {@code date} values (year 1 AD to 5874897 AD).
     *
     * <p>Restricts to AD dates to avoid JDBC binding issues with BC dates in
     * {@code ps.setDate(Date.valueOf(bcDate))}.  Use for text round-trip tests.
     */
    public static Stream<Arguments> datesAD() {
        var r = rng();
        return Stream.generate(() -> {
            long day = r.nextLong(DATE_AD_EPOCH_DAY, DATE_MAX_EPOCH_DAY);
            return Arguments.of(LocalDate.ofEpochDay(day));
        }).limit(COUNT);
    }

    /**
     * Arbitrary {@code time} values with microsecond precision
     * ({@code 00:00:00} to {@code 23:59:59.999999}).
     */
    public static Stream<Arguments> times() {
        var r = rng();
        // Max microseconds in a day: 86399_999999
        return Stream.generate(() -> {
            long micros = r.nextLong(0L, 86_399_999_999L);
            return Arguments.of(LocalTime.ofNanoOfDay(micros * 1_000L));
        }).limit(COUNT);
    }

    /**
     * Arbitrary {@code timetz} values: a local time with microsecond precision
     * paired with a UTC offset in the range [−15:00:00, +15:00:00].
     */
    public static Stream<Arguments> timetzes() {
        var r = rng();
        return Stream.generate(() -> {
            long micros = r.nextLong(0L, 86_399_999_999L);
            LocalTime lt = LocalTime.ofNanoOfDay(micros * 1_000L);
            // Offsets must be whole seconds; range ±54000 s (= ±15 h).
            int tzSecs = r.nextInt(-54_000, 54_000);
            ZoneOffset tz = ZoneOffset.ofTotalSeconds(tzSecs);
            return Arguments.of(lt.atOffset(tz));
        }).limit(COUNT);
    }

    /**
     * Arbitrary {@code timestamp} values spanning PostgreSQL's documented range:
     * 4713 BC to 294276 AD, with microsecond precision.
     *
     * <p>Use this generator for binary round-trip tests.  For text round-trip
     * tests use {@link #timestampsAD()} which restricts to AD dates.
     */
    public static Stream<Arguments> timestamps() {
        var r = rng();
        return Stream.generate(() -> {
            long epochDay = r.nextLong(TIMESTAMP_MIN_EPOCH_DAY, TIMESTAMP_MAX_EPOCH_DAY);
            long micros = r.nextLong(0L, 86_399_999_999L);
            LocalDate date = LocalDate.ofEpochDay(epochDay);
            LocalTime time = LocalTime.ofNanoOfDay(micros * 1_000L);
            return Arguments.of(LocalDateTime.of(date, time));
        }).limit(COUNT);
    }

    /**
     * Arbitrary AD-only {@code timestamp} values (year 1 AD to 294276 AD, microsecond precision).
     *
     * <p>Restricts to AD dates to avoid JDBC binding issues with BC dates in
     * {@code ps.setTimestamp(Timestamp.valueOf(bcDateTime))}.  Use for text round-trip tests.
     */
    public static Stream<Arguments> timestampsAD() {
        var r = rng();
        return Stream.generate(() -> {
            long epochDay = r.nextLong(DATE_AD_EPOCH_DAY, TIMESTAMP_MAX_EPOCH_DAY);
            long micros = r.nextLong(0L, 86_399_999_999L);
            LocalDate date = LocalDate.ofEpochDay(epochDay);
            LocalTime time = LocalTime.ofNanoOfDay(micros * 1_000L);
            return Arguments.of(LocalDateTime.of(date, time));
        }).limit(COUNT);
    }

    /**
     * Arbitrary {@code timestamptz} values normalised to UTC, since PostgreSQL
     * stores {@code timestamptz} as a UTC instant and the binary decoder returns
     * a UTC {@link OffsetDateTime}. Equality-based round-trip assertions
     * therefore require UTC input values.
     *
     * <p>Use this generator for binary round-trip tests.  For text round-trip
     * tests use {@link #timestamptzADs()} which restricts to AD dates.
     */
    public static Stream<Arguments> timestamptzs() {
        var r = rng();
        return Stream.generate(() -> {
            long epochDay = r.nextLong(TIMESTAMP_MIN_EPOCH_DAY, TIMESTAMP_MAX_EPOCH_DAY);
            long micros = r.nextLong(0L, 86_399_999_999L);
            LocalDate date = LocalDate.ofEpochDay(epochDay);
            LocalTime time = LocalTime.ofNanoOfDay(micros * 1_000L);
            return Arguments.of(LocalDateTime.of(date, time).atOffset(ZoneOffset.UTC));
        }).limit(COUNT);
    }

    /**
     * Arbitrary AD-only UTC {@code timestamptz} values (year 1 AD to 294276 AD).
     *
     * <p>Restricts to AD dates to avoid JDBC binding issues with BC dates in
     * {@code ps.setObject(bcOffsetDateTime, TIMESTAMP_WITH_TIMEZONE)}.
     * Use for text round-trip tests.
     */
    public static Stream<Arguments> timestamptzADs() {
        var r = rng();
        return Stream.generate(() -> {
            long epochDay = r.nextLong(DATE_AD_EPOCH_DAY, TIMESTAMP_MAX_EPOCH_DAY);
            long micros = r.nextLong(0L, 86_399_999_999L);
            LocalDate date = LocalDate.ofEpochDay(epochDay);
            LocalTime time = LocalTime.ofNanoOfDay(micros * 1_000L);
            return Arguments.of(LocalDateTime.of(date, time).atOffset(ZoneOffset.UTC));
        }).limit(COUNT);
    }

    // -----------------------------------------------------------------------
    // UUID
    // -----------------------------------------------------------------------

    /** Arbitrary {@code uuid} values (random 128-bit UUIDs). */
    public static Stream<Arguments> uuids() {
        var rnd = new Random();
        return Stream.generate(() -> Arguments.of(
                new UUID(rnd.nextLong(), rnd.nextLong()))).limit(COUNT);
    }

    // -----------------------------------------------------------------------
    // OID
    // -----------------------------------------------------------------------

    /** Arbitrary {@code oid} values in the PostgreSQL valid range [0, 2³²−1]. */
    public static Stream<Arguments> oids() {
        var r = rng();
        return Stream.generate(() -> Arguments.of(
                r.nextLong(0L, 4_294_967_295L))).limit(COUNT);
    }

    // -----------------------------------------------------------------------
    // Network address types
    // -----------------------------------------------------------------------

    /**
     * Arbitrary {@code inet} values covering both IPv4 and IPv6 addresses.
     *
     * <p>IPv4 addresses use masks 0–32; IPv6 addresses use masks 0–128.
     */
    public static Stream<Arguments> inets() {
        var r = rng();
        return Stream.generate(() -> Arguments.of(arbitraryInet(r))).limit(COUNT);
    }

    /**
     * Arbitrary IPv4 {@code inet} values with a canonical mask.
     *
     * <p>IPv4-only variant used for binary round-trip tests because the binary
     * decoder emits the compressed canonical form; IPv4 addresses are already in
     * canonical form and do not require compression.
     */
    public static Stream<Arguments> inetIpv4s() {
        var r = rng();
        return Stream.generate(() -> {
            int mask = r.nextInt(0, 32);
            return Arguments.of(String.format("%d.%d.%d.%d/%d",
                    r.nextInt(0, 255), r.nextInt(0, 255),
                    r.nextInt(0, 255), r.nextInt(0, 255), mask));
        }).limit(COUNT);
    }

    /**
     * Arbitrary IPv4 {@code cidr} values with the host bits zeroed.
     *
     * <p>IPv4-only variant used for binary round-trip tests.
     */
    public static Stream<Arguments> cidrIpv4s() {
        var r = rng();
        return Stream.generate(() -> {
            int mask = r.nextInt(0, 32);
            int addr = r.nextInt(0, Integer.MAX_VALUE) & (int) (0xFFFFFFFFL << (32 - mask));
            return Arguments.of(String.format("%d.%d.%d.%d/%d",
                    (addr >> 24) & 0xFF, (addr >> 16) & 0xFF,
                    (addr >> 8) & 0xFF, addr & 0xFF, mask));
        }).limit(COUNT);
    }

    /**
     * Arbitrary {@code cidr} values covering both IPv4 and IPv6 network prefixes.
     */
    public static Stream<Arguments> cidrs() {
        var r = rng();
        return Stream.generate(() -> Arguments.of(arbitraryCidr(r))).limit(COUNT);
    }

    /** Arbitrary {@code macaddr} values (six colon-separated hex bytes). */
    public static Stream<Arguments> macaddrs() {
        var r = rng();
        return Stream.generate(() -> {
            byte[] b = new byte[6];
            for (int i = 0; i < 6; i++) b[i] = r.nextByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
            return Arguments.of(String.format("%02x:%02x:%02x:%02x:%02x:%02x",
                    b[0] & 0xFF, b[1] & 0xFF, b[2] & 0xFF,
                    b[3] & 0xFF, b[4] & 0xFF, b[5] & 0xFF));
        }).limit(COUNT);
    }

    /** Arbitrary {@code macaddr8} values (eight colon-separated hex bytes). */
    public static Stream<Arguments> macaddr8s() {
        var r = rng();
        return Stream.generate(() -> {
            byte[] b = new byte[8];
            for (int i = 0; i < 8; i++) b[i] = r.nextByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
            return Arguments.of(String.format("%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x",
                    b[0] & 0xFF, b[1] & 0xFF, b[2] & 0xFF, b[3] & 0xFF,
                    b[4] & 0xFF, b[5] & 0xFF, b[6] & 0xFF, b[7] & 0xFF));
        }).limit(COUNT);
    }

    private static String arbitraryInet(SourceOfRandomness r) {
        if (r.nextBoolean()) {
            // IPv4
            return String.format("%d.%d.%d.%d/%d",
                    r.nextInt(0, 255), r.nextInt(0, 255),
                    r.nextInt(0, 255), r.nextInt(0, 255),
                    r.nextInt(0, 32));
        } else {
            // IPv6
            return String.format("%04x:%04x:%04x:%04x:%04x:%04x:%04x:%04x/%d",
                    r.nextInt(0, 0xFFFF), r.nextInt(0, 0xFFFF),
                    r.nextInt(0, 0xFFFF), r.nextInt(0, 0xFFFF),
                    r.nextInt(0, 0xFFFF), r.nextInt(0, 0xFFFF),
                    r.nextInt(0, 0xFFFF), r.nextInt(0, 0xFFFF),
                    r.nextInt(0, 128));
        }
    }

    private static String arbitraryCidr(SourceOfRandomness r) {
        if (r.nextBoolean()) {
            // IPv4 CIDR: mask bits determine how many host bits must be zero
            int mask = r.nextInt(0, 32);
            int addr = r.nextInt(0, (int) ((1L << (32 - mask)) - 1));
            addr <<= (32 - mask);
            return String.format("%d.%d.%d.%d/%d",
                    (addr >> 24) & 0xFF, (addr >> 16) & 0xFF,
                    (addr >> 8) & 0xFF, addr & 0xFF, mask);
        } else {
            // IPv6 CIDR: prefix bits, remaining bits zeroed
            int mask = r.nextInt(0, 128);
            int[] groups = new int[8];
            int fullGroups = mask / 16;
            int remainBits = mask % 16;
            for (int i = 0; i < fullGroups; i++) {
                groups[i] = r.nextInt(0, 0xFFFF);
            }
            if (fullGroups < 8 && remainBits > 0) {
                int m = ((1 << remainBits) - 1) << (16 - remainBits);
                groups[fullGroups] = r.nextInt(0, m) & m;
            }
            return String.format("%04x:%04x:%04x:%04x:%04x:%04x:%04x:%04x/%d",
                    groups[0], groups[1], groups[2], groups[3],
                    groups[4], groups[5], groups[6], groups[7], mask);
        }
    }

    // -----------------------------------------------------------------------
    // Geometric types
    // -----------------------------------------------------------------------

    /** Arbitrary {@code point} values. */
    public static Stream<Arguments> points() {
        var r = rng();
        return Stream.generate(() ->
                Arguments.of(new PGpoint(r.nextDouble(-1e6, 1e6), r.nextDouble(-1e6, 1e6)))
        ).limit(COUNT);
    }

    /**
     * Arbitrary {@code line} values (infinite line described by {@code ax+by+c=0}).
     *
     * <p>At least one of {@code a} or {@code b} must be non-zero.
     */
    public static Stream<Arguments> lines() {
        var r = rng();
        return Stream.generate(() -> {
            double a, b;
            do {
                a = r.nextDouble(-10.0, 10.0);
                b = r.nextDouble(-10.0, 10.0);
            } while (a == 0.0 && b == 0.0);
            double c = r.nextDouble(-10.0, 10.0);
            try {
                return Arguments.of(new PGline(a, b, c));
            } catch (Exception e) {
                return Arguments.of(new PGline(1.0, 0.0, 0.0));
            }
        }).limit(COUNT);
    }

    /** Arbitrary {@code lseg} values (line segments). */
    public static Stream<Arguments> lsegs() {
        var r = rng();
        return Stream.generate(() -> {
            var p1 = new PGpoint(r.nextDouble(-1e6, 1e6), r.nextDouble(-1e6, 1e6));
            var p2 = new PGpoint(r.nextDouble(-1e6, 1e6), r.nextDouble(-1e6, 1e6));
            return Arguments.of(new PGlseg(p1, p2));
        }).limit(COUNT);
    }

    /** Arbitrary {@code box} values. */
    public static Stream<Arguments> boxes() {
        var r = rng();
        return Stream.generate(() -> {
            double x1 = r.nextDouble(-1e6, 1e6), y1 = r.nextDouble(-1e6, 1e6);
            double x2 = r.nextDouble(-1e6, 1e6), y2 = r.nextDouble(-1e6, 1e6);
            var upper = new PGpoint(Math.max(x1, x2), Math.max(y1, y2));
            var lower = new PGpoint(Math.min(x1, x2), Math.min(y1, y2));
            return Arguments.of(new PGbox(upper, lower));
        }).limit(COUNT);
    }

    /**
     * Arbitrary {@code path} values (open or closed, 2–5 points).
     *
     * <p>Closed paths are analogous to PostgreSQL's closed-path syntax
     * {@code ((x1,y1),...)}; open paths use {@code [(x1,y1),...]}.
     */
    public static Stream<Arguments> paths() {
        var r = rng();
        return Stream.generate(() -> {
            int n = r.nextInt(2, 5);
            boolean isOpen = r.nextBoolean();
            var pts = new PGpoint[n];
            for (int i = 0; i < n; i++) {
                pts[i] = new PGpoint(r.nextDouble(-1e4, 1e4), r.nextDouble(-1e4, 1e4));
            }
            return Arguments.of(new PGpath(pts, isOpen));
        }).limit(COUNT);
    }

    /**
     * Arbitrary {@code polygon} values (2–5 vertices).
     */
    public static Stream<Arguments> polygons() {
        var r = rng();
        return Stream.generate(() -> {
            int n = r.nextInt(2, 5);
            var pts = new PGpoint[n];
            for (int i = 0; i < n; i++) {
                pts[i] = new PGpoint(r.nextDouble(-1e4, 1e4), r.nextDouble(-1e4, 1e4));
            }
            return Arguments.of(new PGpolygon(pts));
        }).limit(COUNT);
    }

    /**
     * Arbitrary {@code circle} values.
     *
     * <p>The radius is always non-negative, matching PostgreSQL's constraint
     * (and the Haskell {@code Circle} Arbitrary instance).
     */
    public static Stream<Arguments> circles() {
        var r = rng();
        return Stream.generate(() -> {
            double cx = r.nextDouble(-1e6, 1e6);
            double cy = r.nextDouble(-1e6, 1e6);
            double radius = Math.abs(r.nextDouble(-1e6, 1e6));
            var center = new PGpoint(cx, cy);
            return Arguments.of(new PGcircle(center, radius));
        }).limit(COUNT);
    }

    // -----------------------------------------------------------------------
    // Bit-string types
    // -----------------------------------------------------------------------

    /**
     * Arbitrary {@code bit} values (fixed-length bit strings of 1–64 bits).
     *
     * <p>The length is fixed per value as PostgreSQL {@code bit(n)} is
     * fixed-width; we vary {@code n} across samples.
     */
    public static Stream<Arguments> bits() {
        var r = rng();
        return Stream.generate(() -> Arguments.of(arbitraryBitString(r, r.nextInt(1, 64)))).limit(COUNT);
    }

    /** Arbitrary {@code varbit} values (variable-length bit strings, 0–64 bits). */
    public static Stream<Arguments> varbits() {
        var r = rng();
        return Stream.generate(() -> Arguments.of(arbitraryBitString(r, r.nextInt(0, 64)))).limit(COUNT);
    }

    private static String arbitraryBitString(SourceOfRandomness r, int len) {
        var sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(r.nextBoolean() ? '1' : '0');
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // JSON types
    // -----------------------------------------------------------------------

    /**
     * Arbitrary {@code json} / {@code jsonb} values.
     *
     * <p>Generates simple well-formed JSON literals (objects, arrays, strings,
     * numbers, booleans, null) that are accepted by PostgreSQL.
     */
    public static Stream<Arguments> jsons() {
        var r = rng();
        return Stream.generate(() -> Arguments.of(arbitraryJson(r))).limit(COUNT);
    }

    public static Stream<Arguments> jsonbs() {
        return jsons();
    }

    private static String arbitraryJson(SourceOfRandomness r) {
        return switch (r.nextInt(0, 4)) {
            case 0 -> {
                // JSON object with 0–3 string keys
                int n = r.nextInt(0, 3);
                var sb = new StringBuilder("{");
                for (int i = 0; i < n; i++) {
                    if (i > 0) sb.append(',');
                    sb.append('"').append(arbitraryAlpha(r, 1, 8)).append('"');
                    sb.append(':');
                    sb.append(arbitraryJsonScalar(r));
                }
                sb.append('}');
                yield sb.toString();
            }
            case 1 -> {
                // JSON array with 0–3 elements
                int n = r.nextInt(0, 3);
                var sb = new StringBuilder("[");
                for (int i = 0; i < n; i++) {
                    if (i > 0) sb.append(',');
                    sb.append(arbitraryJsonScalar(r));
                }
                sb.append(']');
                yield sb.toString();
            }
            default -> arbitraryJsonScalar(r);
        };
    }

    private static String arbitraryJsonScalar(SourceOfRandomness r) {
        return switch (r.nextInt(0, 4)) {
            case 0 -> '"' + arbitraryAlpha(r, 0, 10) + '"';
            case 1 -> String.valueOf(r.nextInt(-1000, 1000));
            case 2 -> r.nextBoolean() ? "true" : "false";
            default -> "null";
        };
    }

    private static String arbitraryAlpha(SourceOfRandomness r, int minLen, int maxLen) {
        int len = r.nextInt(minLen, maxLen);
        var sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(r.nextChar('a', 'z'));
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Tsvector
    // -----------------------------------------------------------------------

    /**
     * Arbitrary {@code tsvector} values in PostgreSQL's canonical form:
     * a space-separated list of single-quoted lowercase lexemes.
     *
     * <p>We use the canonical quoted form so that the round-trip string
     * comparison is stable.
     */
    public static Stream<Arguments> tsvectors() {
        var r = rng();
        return Stream.generate(() -> {
            int n = r.nextInt(1, 5);
            var lexemes = new ArrayList<String>(n);
            for (int i = 0; i < n; i++) {
                lexemes.add(arbitraryAlpha(r, 1, 10));
            }
            // Sort and deduplicate to produce the canonical tsvector form.
            lexemes.sort(null);
            var unique = lexemes.stream().distinct().toList();
            var sb = new StringBuilder();
            for (int i = 0; i < unique.size(); i++) {
                if (i > 0) sb.append(' ');
                sb.append('\'').append(unique.get(i)).append('\'');
            }
            return Arguments.of(sb.toString());
        }).limit(COUNT);
    }

    // -----------------------------------------------------------------------
    // Interval
    // -----------------------------------------------------------------------

    /**
     * Arbitrary {@code interval} values in PostgreSQL's verbose output format.
     *
     * <p>We generate intervals with at most one non-zero component so that the
     * PostgreSQL round-trip is stable and the result string can be predicted.
     */
    public static Stream<Arguments> intervals() {
        var r = rng();
        return Stream.generate(() -> {
            // Pick one component to keep the format predictable.
            int component = r.nextInt(0, 5);
            int n = r.nextInt(0, 99);
            String val = switch (component) {
                case 0 -> n + " year" + (n == 1 ? "" : "s");
                case 1 -> n + " mon" + (n == 1 ? "" : "s");
                case 2 -> n + " day" + (n == 1 ? "" : "s");
                case 3 -> String.format("%02d:00:00", n % 100);
                default -> String.format("00:00:%02d", n % 60);
            };
            return Arguments.of(val);
        }).limit(COUNT);
    }

}
