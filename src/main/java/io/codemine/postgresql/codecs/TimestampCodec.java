package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

/** Codec for PostgreSQL {@code timestamp} values (microseconds from 2000-01-01 00:00:00). */
final class TimestampCodec implements Codec<Long> {

  // Unix epoch seconds at 2000-01-01T00:00:00 UTC
  static final long PG_EPOCH_UNIX_SECONDS = 946_684_800L;
  static final long PG_EPOCH_UNIX_MICROS = PG_EPOCH_UNIX_SECONDS * 1_000_000L;

  @Override
  public String name() {
    return "timestamp";
  }

  @Override
  public int scalarOid() {
    return 1114;
  }

  @Override
  public int arrayOid() {
    return 1115;
  }

  @Override
  public void write(StringBuilder sb, Long value) {
    writeTimestamp(sb, value);
  }

  @Override
  public Codec.ParsingResult<Long> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      long pgMicros = parseTimestamp(s);
      return new Codec.ParsingResult<>(pgMicros, input.length());
    } catch (Exception e) {
      throw new Codec.DecodingException(input, offset, "Invalid timestamp: " + s);
    }
  }

  @Override
  public void encodeInBinary(Long value, ByteArrayOutputStream out) {
    long v = value;
    out.write((int) (v >>> 56) & 0xFF);
    out.write((int) (v >>> 48) & 0xFF);
    out.write((int) (v >>> 40) & 0xFF);
    out.write((int) (v >>> 32) & 0xFF);
    out.write((int) (v >>> 24) & 0xFF);
    out.write((int) (v >>> 16) & 0xFF);
    out.write((int) (v >>> 8) & 0xFF);
    out.write((int) (v & 0xFF));
  }

  @Override
  public Long decodeInBinary(ByteBuffer buf, int length) {
    return buf.getLong();
  }

  @Override
  public Long random(Random r, int size) {
    if (size == 0) return 0L;
    long bound = (long) size * 86_400_000_000L;
    return r.nextLong(-bound, bound + 1);
  }

  /** Writes a PG timestamp (microseconds from 2000-01-01) as text. */
  static void writeTimestamp(StringBuilder sb, long pgMicros) {
    long unixMicros = pgMicros + PG_EPOCH_UNIX_MICROS;
    long epochSecond = Math.floorDiv(unixMicros, 1_000_000L);
    long microOfSecond = Math.floorMod(unixMicros, 1_000_000L);
    LocalDateTime dt =
        LocalDateTime.ofEpochSecond(epochSecond, (int) (microOfSecond * 1000L), ZoneOffset.UTC);

    sb.append(
        String.format(
            "%04d-%02d-%02d %02d:%02d:%02d",
            dt.getYear(),
            dt.getMonthValue(),
            dt.getDayOfMonth(),
            dt.getHour(),
            dt.getMinute(),
            dt.getSecond()));
    TimeCodec.appendFraction(sb, microOfSecond);
  }

  /** Parses a timestamp string and returns PG microseconds from 2000-01-01. */
  static long parseTimestamp(String s) {
    // Format: YYYY-MM-DD hh:mm:ss[.ffffff][±tz]
    // Split at space to get date and time parts
    int spaceIdx = s.indexOf(' ');
    if (spaceIdx < 0) {
      throw new IllegalArgumentException("Invalid timestamp: " + s);
    }
    String datePart = s.substring(0, spaceIdx);
    String timePart = s.substring(spaceIdx + 1);

    // Parse date
    String[] dateFields = datePart.split("-");
    int year = Integer.parseInt(dateFields[0]);
    int month = Integer.parseInt(dateFields[1]);
    int day = Integer.parseInt(dateFields[2]);

    // Strip timezone if present (for timestamptz parsing reuse)
    int tzOffset = 0;
    int tzStart = findTimezoneStart(timePart);
    if (tzStart >= 0) {
      String tzPart = timePart.substring(tzStart);
      timePart = timePart.substring(0, tzStart);
      tzOffset = parseTimezoneOffset(tzPart);
    }

    // Parse time
    String[] timeFields = timePart.split(":");
    int hour = Integer.parseInt(timeFields[0]);
    int minute = Integer.parseInt(timeFields[1]);
    String secStr = timeFields[2];
    int second;
    long microOfSecond = 0;
    int dot = secStr.indexOf('.');
    if (dot >= 0) {
      second = Integer.parseInt(secStr.substring(0, dot));
      String frac = secStr.substring(dot + 1);
      while (frac.length() < 6) frac = frac + "0";
      if (frac.length() > 6) frac = frac.substring(0, 6);
      microOfSecond = Long.parseLong(frac);
    } else {
      second = Integer.parseInt(secStr);
    }

    LocalDateTime dt = LocalDateTime.of(year, month, day, hour, minute, second);
    long epochSecond = dt.toEpochSecond(ZoneOffset.UTC) - tzOffset;
    long unixMicros = epochSecond * 1_000_000L + microOfSecond;
    return unixMicros - PG_EPOCH_UNIX_MICROS;
  }

  /**
   * Finds timezone start in a time string. Returns -1 if none found. Searches for + or - that
   * indicates a timezone offset.
   */
  static int findTimezoneStart(String s) {
    for (int i = s.length() - 1; i >= 0; i--) {
      char c = s.charAt(i);
      if (c == '+' || c == '-') {
        // Ensure it's not part of the seconds fraction or an exponent
        if (i > 0 && Character.isDigit(s.charAt(i - 1))) {
          return i;
        }
      }
    }
    return -1;
  }

  /** Parses a timezone offset string like "+00", "+05:30", "-03" to seconds east of UTC. */
  static int parseTimezoneOffset(String tz) {
    char sign = tz.charAt(0);
    String abs = tz.substring(1);
    String[] parts = abs.split(":");
    int hours = Integer.parseInt(parts[0]);
    int minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
    int seconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
    int offset = hours * 3600 + minutes * 60 + seconds;
    if (sign == '-') offset = -offset;
    return offset;
  }
}
