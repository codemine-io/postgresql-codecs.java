package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code time} values (microseconds from midnight). */
final class TimeCodec implements Codec<Long> {

  private static final long MAX_TIME = 86_400_000_000L;

  @Override
  public String name() {
    return "time";
  }

  @Override
  public int scalarOid() {
    return 1083;
  }

  @Override
  public int arrayOid() {
    return 1183;
  }

  @Override
  public void write(StringBuilder sb, Long value) {
    writeTime(sb, value);
  }

  @Override
  public Codec.ParsingResult<Long> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      long micros = parseTime(s, 0);
      return new Codec.ParsingResult<>(micros, input.length());
    } catch (Exception e) {
      throw new Codec.DecodingException(input, offset, "Invalid time: " + s);
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
    return r.nextLong(0, MAX_TIME);
  }

  /** Writes a time-of-day in microseconds to the StringBuilder as hh:mm:ss[.ffffff]. */
  static void writeTime(StringBuilder sb, long micros) {
    long total = micros;
    long hours = total / 3_600_000_000L;
    total %= 3_600_000_000L;
    long minutes = total / 60_000_000L;
    total %= 60_000_000L;
    long seconds = total / 1_000_000L;
    long frac = total % 1_000_000L;

    sb.append(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    appendFraction(sb, frac);
  }

  /** Appends fractional seconds (1-6 digits, trailing zeros stripped) if non-zero. */
  static void appendFraction(StringBuilder sb, long micros) {
    if (micros > 0) {
      String f = String.format("%06d", micros);
      int end = f.length();
      while (end > 0 && f.charAt(end - 1) == '0') end--;
      sb.append('.').append(f, 0, end);
    }
  }

  /**
   * Parses a time string (hh:mm:ss[.ffffff]) starting at the given position and returns the time in
   * microseconds.
   */
  static long parseTime(CharSequence input, int pos) {
    String s = input.subSequence(pos, input.length()).toString();
    String[] parts = s.split(":");
    if (parts.length < 3) {
      throw new IllegalArgumentException("Invalid time: " + s);
    }
    long hours = Long.parseLong(parts[0]);
    long minutes = Long.parseLong(parts[1]);
    // seconds may have fractional part
    String secPart = parts[2];
    long seconds;
    long micros = 0;
    int dot = secPart.indexOf('.');
    if (dot >= 0) {
      seconds = Long.parseLong(secPart.substring(0, dot));
      String frac = secPart.substring(dot + 1);
      // Pad to 6 digits
      while (frac.length() < 6) frac = frac + "0";
      if (frac.length() > 6) frac = frac.substring(0, 6);
      micros = Long.parseLong(frac);
    } else {
      seconds = Long.parseLong(secPart);
    }
    return hours * 3_600_000_000L + minutes * 60_000_000L + seconds * 1_000_000L + micros;
  }
}
