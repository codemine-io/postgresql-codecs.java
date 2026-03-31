package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code uuid} values. */
final class UuidCodec implements Codec<java.util.UUID> {

  @Override
  public String name() {
    return "uuid";
  }

  @Override
  public int scalarOid() {
    return 2950;
  }

  @Override
  public int arrayOid() {
    return 2951;
  }

  @Override
  public void encodeInText(StringBuilder sb, java.util.UUID value) {
    sb.append(value.toString());
  }

  @Override
  public Codec.ParsingResult<java.util.UUID> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      java.util.UUID value = java.util.UUID.fromString(s);
      return new Codec.ParsingResult<>(value, input.length());
    } catch (IllegalArgumentException e) {
      throw new Codec.DecodingException(input, offset, "Invalid uuid: " + e.getMessage());
    }
  }

  @Override
  public void encodeInBinary(java.util.UUID value, ByteArrayOutputStream out) {
    long msb = value.getMostSignificantBits();
    long lsb = value.getLeastSignificantBits();
    writeLong(out, msb);
    writeLong(out, lsb);
  }

  @Override
  public java.util.UUID decodeInBinary(ByteBuffer buf, int length) {
    long msb = buf.getLong();
    long lsb = buf.getLong();
    return new java.util.UUID(msb, lsb);
  }

  @Override
  public java.util.UUID random(Random r, int size) {
    return new java.util.UUID(r.nextLong(), r.nextLong());
  }

  private static void writeLong(ByteArrayOutputStream out, long value) {
    out.write((int) (value >>> 56) & 0xFF);
    out.write((int) (value >>> 48) & 0xFF);
    out.write((int) (value >>> 40) & 0xFF);
    out.write((int) (value >>> 32) & 0xFF);
    out.write((int) (value >>> 24) & 0xFF);
    out.write((int) (value >>> 16) & 0xFF);
    out.write((int) (value >>> 8) & 0xFF);
    out.write((int) value & 0xFF);
  }
}
