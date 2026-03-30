package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code money} values. */
final class MoneyCodec implements Codec<Long> {

  @Override
  public String name() {
    return "money";
  }

  @Override
  public int scalarOid() {
    return 790;
  }

  @Override
  public int arrayOid() {
    return 791;
  }

  @Override
  public void write(StringBuilder sb, Long value) {
    boolean negative = value < 0;
    long abs = Math.abs(value);
    if (negative) {
      sb.append("-");
    }
    sb.append("$").append(abs).append(".00");
  }

  @Override
  public Codec.ParsingResult<Long> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      // Handle negative in parentheses: ($1.23) or -$1.23
      boolean negative = false;
      if (s.startsWith("(") && s.endsWith(")")) {
        negative = true;
        s = s.substring(1, s.length() - 1);
      }
      if (s.startsWith("-")) {
        negative = true;
        s = s.substring(1);
      }
      // Strip currency symbol and commas.
      s = s.replace("$", "").replace(",", "");
      // Parse as decimal and convert to Long dollars (ignore fractional cents).
      int dotIndex = s.indexOf('.');
      long value;
      if (dotIndex < 0) {
        value = Long.parseLong(s);
      } else {
        value = Long.parseLong(s.substring(0, dotIndex));
      }
      if (negative) {
        value = -value;
      }
      return new Codec.ParsingResult<>(value, input.length());
    } catch (NumberFormatException e) {
      throw new Codec.DecodingException(input, offset, "Invalid money: " + e.getMessage());
    }
  }

  @Override
  public void encodeInBinary(Long value, ByteArrayOutputStream out) {
    // PostgreSQL money binary format: int64 in cents (smallest currency unit).
    // Our Long represents whole dollars, so multiply by 100.
    long cents = value * 100L;
    out.write((int) (cents >>> 56) & 0xFF);
    out.write((int) (cents >>> 48) & 0xFF);
    out.write((int) (cents >>> 40) & 0xFF);
    out.write((int) (cents >>> 32) & 0xFF);
    out.write((int) (cents >>> 24) & 0xFF);
    out.write((int) (cents >>> 16) & 0xFF);
    out.write((int) (cents >>> 8) & 0xFF);
    out.write((int) (cents & 0xFF));
  }

  @Override
  public Long decodeInBinary(ByteBuffer buf, int length) {
    // PostgreSQL money binary format: int64 in cents. Convert to whole dollars.
    return buf.getLong() / 100L;
  }

  @Override
  public Long random(Random r, int size) {
    if (size == 0) {
      return 0L;
    }
    // Generate whole-dollar amounts so that PG's integer cast (from R2DBC's LongCodec)
    // yields the same dollar value when PG casts the integer as money.
    return r.nextLong(-(long) size, (long) size + 1);
  }
}
