package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code int8} values. */
final class Int8Codec implements Codec<Long> {

  @Override
  public String name() {
    return "int8";
  }

  @Override
  public int scalarOid() {
    return 20;
  }

  @Override
  public int arrayOid() {
    return 1016;
  }

  @Override
  public void encodeInText(StringBuilder sb, Long value) {
    sb.append(value);
  }

  @Override
  public Codec.ParsingResult<Long> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    try {
      long value = Long.parseLong(input.subSequence(offset, input.length()).toString().trim());
      return new Codec.ParsingResult<>(value, input.length());
    } catch (NumberFormatException e) {
      throw new Codec.DecodingException(input, offset, "Invalid int8: " + e.getMessage());
    }
  }

  @Override
  public void encodeInBinary(Long value, ByteArrayOutputStream out) {
    out.write((int) (value >>> 56) & 0xFF);
    out.write((int) (value >>> 48) & 0xFF);
    out.write((int) (value >>> 40) & 0xFF);
    out.write((int) (value >>> 32) & 0xFF);
    out.write((int) (value >>> 24) & 0xFF);
    out.write((int) (value >>> 16) & 0xFF);
    out.write((int) (value >>> 8) & 0xFF);
    out.write((int) (value & 0xFF));
  }

  @Override
  public Long decodeInBinary(ByteBuffer buf, int length) {
    return buf.getLong();
  }

  @Override
  public Long random(Random r, int size) {
    if (size == 0) {
      return 0L;
    }
    return r.nextLong(-(long) size, (long) size + 1);
  }
}
