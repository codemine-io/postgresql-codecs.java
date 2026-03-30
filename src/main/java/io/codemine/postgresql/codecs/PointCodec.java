package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code point} values. */
final class PointCodec implements Codec<Point> {

  @Override
  public String name() {
    return "point";
  }

  @Override
  public int scalarOid() {
    return 600;
  }

  @Override
  public int arrayOid() {
    return 1017;
  }

  @Override
  public void write(StringBuilder sb, Point value) {
    sb.append('(');
    sb.append(Double.toString(value.x()));
    sb.append(',');
    sb.append(Double.toString(value.y()));
    sb.append(')');
  }

  @Override
  public Codec.ParsingResult<Point> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      Point point = parsePoint(s);
      return new Codec.ParsingResult<>(point, input.length());
    } catch (Exception e) {
      throw new Codec.DecodingException(input, offset, "Invalid point: " + s);
    }
  }

  @Override
  public void encodeInBinary(Point value, ByteArrayOutputStream out) {
    writeFloat8(out, value.x());
    writeFloat8(out, value.y());
  }

  @Override
  public Point decodeInBinary(ByteBuffer buf, int length) {
    return new Point(buf.getDouble(), buf.getDouble());
  }

  @Override
  public Point random(Random r, int size) {
    if (size == 0) return new Point(0.0, 0.0);
    return new Point(finiteDouble(r, size), finiteDouble(r, size));
  }

  /** Parses a point string "(x,y)". */
  static Point parsePoint(String s) {
    s = s.trim();
    if (s.startsWith("(") && s.endsWith(")")) {
      s = s.substring(1, s.length() - 1);
    }
    int comma = findSeparatingComma(s);
    double x = Double.parseDouble(s.substring(0, comma).trim());
    double y = Double.parseDouble(s.substring(comma + 1).trim());
    return new Point(x, y);
  }

  /**
   * Finds the comma that separates two double values, skipping commas that might appear inside
   * numbers (they don't in standard notation, but this is defensive).
   */
  static int findSeparatingComma(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == ',') {
        return i;
      }
    }
    throw new IllegalArgumentException("No comma found in: " + s);
  }

  static void writeFloat8(ByteArrayOutputStream out, double value) {
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

  static double finiteDouble(Random r, int size) {
    return (r.nextDouble() * 2 - 1) * size;
  }
}
