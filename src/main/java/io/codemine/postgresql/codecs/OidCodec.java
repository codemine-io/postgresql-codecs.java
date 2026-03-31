package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code oid} values. */
final class OidCodec implements Codec<Integer> {

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
  public void render(StringBuilder sb, Integer value) {
    sb.append(Integer.toUnsignedString(value));
  }

  @Override
  public Codec.ParsingResult<Integer> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    try {
      int value =
          Integer.parseUnsignedInt(input.subSequence(offset, input.length()).toString().trim());
      return new Codec.ParsingResult<>(value, input.length());
    } catch (NumberFormatException e) {
      throw new Codec.DecodingException(input, offset, "Invalid oid: " + e.getMessage());
    }
  }

  @Override
  public void encodeInBinary(Integer value, ByteArrayOutputStream out) {
    out.write((value >>> 24) & 0xFF);
    out.write((value >>> 16) & 0xFF);
    out.write((value >>> 8) & 0xFF);
    out.write(value & 0xFF);
  }

  @Override
  public Integer decodeInBinary(ByteBuffer buf, int length) {
    return buf.getInt();
  }

  @Override
  public Integer random(Random r, int size) {
    if (size == 0) {
      return 0;
    }
    return r.nextInt(size);
  }
}
