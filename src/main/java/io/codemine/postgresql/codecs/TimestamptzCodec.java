package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code timestamptz} values (microseconds from 2000-01-01 00:00:00 UTC). */
final class TimestamptzCodec implements Codec<Long> {

  @Override
  public String name() {
    return "timestamptz";
  }

  @Override
  public int scalarOid() {
    return 1184;
  }

  @Override
  public int arrayOid() {
    return 1185;
  }

  @Override
  public void write(StringBuilder sb, Long value) {
    TimestampCodec.writeTimestamp(sb, value);
    sb.append("+00");
  }

  @Override
  public Codec.ParsingResult<Long> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      long pgMicros = TimestampCodec.parseTimestamp(s);
      return new Codec.ParsingResult<>(pgMicros, input.length());
    } catch (Exception e) {
      throw new Codec.DecodingException(input, offset, "Invalid timestamptz: " + s);
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
}
