package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code circle} values. */
final class CircleCodec implements Codec<Circle> {

  @Override
  public String name() {
    return "circle";
  }

  @Override
  public int scalarOid() {
    return 718;
  }

  @Override
  public int arrayOid() {
    return 719;
  }

  @Override
  public void write(StringBuilder sb, Circle value) {
    sb.append("<(");
    sb.append(Double.toString(value.x()));
    sb.append(',');
    sb.append(Double.toString(value.y()));
    sb.append("),");
    sb.append(Double.toString(value.r()));
    sb.append('>');
  }

  @Override
  public Codec.ParsingResult<Circle> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      // Format: <(x,y),r>
      if (s.startsWith("<") && s.endsWith(">")) {
        s = s.substring(1, s.length() - 1);
      }
      // Now: (x,y),r
      // Find the closing paren of the point
      int closeParen = s.indexOf(')');
      String pointStr = s.substring(0, closeParen + 1);
      // After the point there's a comma then r
      String rStr = s.substring(closeParen + 2).trim();
      Point center = PointCodec.parsePoint(pointStr);
      double r = Double.parseDouble(rStr);
      return new Codec.ParsingResult<>(new Circle(center.x(), center.y(), r), input.length());
    } catch (Exception e) {
      throw new Codec.DecodingException(input, offset, "Invalid circle: " + s);
    }
  }

  @Override
  public void encodeInBinary(Circle value, ByteArrayOutputStream out) {
    PointCodec.writeFloat8(out, value.x());
    PointCodec.writeFloat8(out, value.y());
    PointCodec.writeFloat8(out, value.r());
  }

  @Override
  public Circle decodeInBinary(ByteBuffer buf, int length) {
    return new Circle(buf.getDouble(), buf.getDouble(), buf.getDouble());
  }

  @Override
  public Circle random(Random r, int size) {
    if (size == 0) return new Circle(0.0, 0.0, 0.0);
    return new Circle(
        PointCodec.finiteDouble(r, size), PointCodec.finiteDouble(r, size), r.nextDouble() * size);
  }
}
