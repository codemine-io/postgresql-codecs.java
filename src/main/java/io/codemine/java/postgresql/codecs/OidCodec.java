package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code oid} values. */
final class OidCodec implements Codec<Long> {

  private static final long MAX_OID = 0xFFFFFFFFL;

  @Override
  public String name() {
    return "oid";
  }

  @Override
  public int scalarOid() {
    return 26;
  }

  @Override
  public int arrayOid() {
    return 1028;
  }

  @Override
  public void encodeInText(StringBuilder sb, Long value) {
    sb.append(Long.toUnsignedString(value));
  }

  @Override
  public Codec.ParsingResult<Long> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    try {
      long value =
          Long.parseUnsignedLong(input.subSequence(offset, input.length()).toString().trim());
      if (value > MAX_OID) {
        throw new Codec.DecodingException(input, offset, "OID out of range: " + value);
      }
      return new Codec.ParsingResult<>(value, input.length());
    } catch (NumberFormatException e) {
      throw new Codec.DecodingException(input, offset, "Invalid oid: " + e.getMessage());
    }
  }

  @Override
  public void encodeInBinary(Long value, ByteArrayOutputStream out) {
    if (value < 0 || value > MAX_OID) {
      throw new IllegalArgumentException("OID out of range: " + value);
    }
    out.write((int) ((value >>> 24) & 0xFF));
    out.write((int) ((value >>> 16) & 0xFF));
    out.write((int) ((value >>> 8) & 0xFF));
    out.write((int) (value & 0xFF));
  }

  @Override
  public Long decodeInBinary(ByteBuffer buf, int length) {
    return buf.getInt() & 0xFFFFFFFFL;
  }

  @Override
  public Long random(Random r, int size) {
    if (size == 0) {
      return 0L;
    }
    return (long) r.nextInt(size);
  }
}
