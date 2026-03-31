package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code int2} values. */
final class Int2Codec implements Codec<Short> {

  @Override
  public String name() {
    return "int2";
  }

  @Override
  public int scalarOid() {
    return 21;
  }

  @Override
  public int arrayOid() {
    return 1005;
  }

  @Override
  public void encodeInText(StringBuilder sb, Short value) {
    sb.append(value);
  }

  @Override
  public Codec.ParsingResult<Short> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    try {
      short value = Short.parseShort(input.subSequence(offset, input.length()).toString().trim());
      return new Codec.ParsingResult<>(value, input.length());
    } catch (NumberFormatException e) {
      throw new Codec.DecodingException(input, offset, "Invalid int2: " + e.getMessage());
    }
  }

  @Override
  public void encodeInBinary(Short value, ByteArrayOutputStream out) {
    out.write((value >>> 8) & 0xFF);
    out.write(value & 0xFF);
  }

  @Override
  public Short decodeInBinary(ByteBuffer buf, int length) {
    return buf.getShort();
  }

  @Override
  public Short random(Random r, int size) {
    if (size == 0) {
      return 0;
    }
    int bound = Math.min(size, Short.MAX_VALUE);
    return (short) (r.nextInt(2 * bound + 1) - bound);
  }
}
