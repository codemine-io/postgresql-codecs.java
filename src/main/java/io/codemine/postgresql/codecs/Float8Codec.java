package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code float8} values. */
final class Float8Codec implements Codec<Double> {

  @Override
  public String name() {
    return "float8";
  }

  @Override
  public int scalarOid() {
    return 701;
  }

  @Override
  public int arrayOid() {
    return 1022;
  }

  @Override
  public void write(StringBuilder sb, Double value) {
    if (Double.isNaN(value)) {
      sb.append("NaN");
    } else if (value == Double.POSITIVE_INFINITY) {
      sb.append("Infinity");
    } else if (value == Double.NEGATIVE_INFINITY) {
      sb.append("-Infinity");
    } else {
      sb.append(value);
    }
  }

  @Override
  public Codec.ParsingResult<Double> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      String normalized = s.toLowerCase();
      double value =
          switch (normalized) {
            case "nan" -> Double.NaN;
            case "infinity" -> Double.POSITIVE_INFINITY;
            case "-infinity" -> Double.NEGATIVE_INFINITY;
            default -> Double.parseDouble(s);
          };
      return new Codec.ParsingResult<>(value, input.length());
    } catch (NumberFormatException e) {
      throw new Codec.DecodingException(input, offset, "Invalid float8: " + e.getMessage());
    }
  }

  @Override
  public void encodeInBinary(Double value, ByteArrayOutputStream out) {
    long bits = Double.doubleToLongBits(value);
    out.write((int) (bits >>> 56) & 0xFF);
    out.write((int) (bits >>> 48) & 0xFF);
    out.write((int) (bits >>> 40) & 0xFF);
    out.write((int) (bits >>> 32) & 0xFF);
    out.write((int) (bits >>> 24) & 0xFF);
    out.write((int) (bits >>> 16) & 0xFF);
    out.write((int) (bits >>> 8) & 0xFF);
    out.write((int) bits & 0xFF);
  }

  @Override
  public Double decodeInBinary(ByteBuffer buf, int length) {
    return Double.longBitsToDouble(buf.getLong());
  }

  @Override
  public Double random(Random r, int size) {
    if (size == 0) {
      return 0.0;
    }
    int choice = r.nextInt(20);
    if (choice == 0) {
      return Double.NaN;
    } else if (choice == 1) {
      return Double.POSITIVE_INFINITY;
    } else if (choice == 2) {
      return Double.NEGATIVE_INFINITY;
    }
    return (r.nextDouble() * 2 - 1) * size;
  }
}
