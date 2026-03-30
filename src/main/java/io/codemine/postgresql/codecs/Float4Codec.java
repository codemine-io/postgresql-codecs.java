package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code float4} values. */
final class Float4Codec implements Codec<Float> {

  @Override
  public String name() {
    return "float4";
  }

  @Override
  public int scalarOid() {
    return 700;
  }

  @Override
  public int arrayOid() {
    return 1021;
  }

  @Override
  public void write(StringBuilder sb, Float value) {
    if (Float.isNaN(value)) {
      sb.append("NaN");
    } else if (value == Float.POSITIVE_INFINITY) {
      sb.append("Infinity");
    } else if (value == Float.NEGATIVE_INFINITY) {
      sb.append("-Infinity");
    } else {
      sb.append(value);
    }
  }

  @Override
  public Codec.ParsingResult<Float> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      float value =
          switch (s.toLowerCase()) {
            case "nan" -> Float.NaN;
            case "infinity" -> Float.POSITIVE_INFINITY;
            case "-infinity" -> Float.NEGATIVE_INFINITY;
            default -> Float.parseFloat(s);
          };
      return new Codec.ParsingResult<>(value, input.length());
    } catch (NumberFormatException e) {
      throw new Codec.DecodingException(input, offset, "Invalid float4: " + e.getMessage());
    }
  }

  @Override
  public void encodeInBinary(Float value, ByteArrayOutputStream out) {
    int bits = Float.floatToIntBits(value);
    out.write((bits >>> 24) & 0xFF);
    out.write((bits >>> 16) & 0xFF);
    out.write((bits >>> 8) & 0xFF);
    out.write(bits & 0xFF);
  }

  @Override
  public Float decodeInBinary(ByteBuffer buf, int length) {
    return Float.intBitsToFloat(buf.getInt());
  }

  @Override
  public Float random(Random r, int size) {
    if (size == 0) {
      return 0.0f;
    }
    int choice = r.nextInt(20);
    if (choice == 0) {
      return Float.NaN;
    } else if (choice == 1) {
      return Float.POSITIVE_INFINITY;
    } else if (choice == 2) {
      return Float.NEGATIVE_INFINITY;
    }
    return (r.nextFloat() * 2 - 1) * size;
  }
}
