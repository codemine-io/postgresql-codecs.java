package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code box} values. */
final class BoxCodec implements Codec<Box> {

  @Override
  public String name() {
    return "box";
  }

  @Override
  public int scalarOid() {
    return 603;
  }

  @Override
  public int arrayOid() {
    return 1020;
  }

  @Override
  public void write(StringBuilder sb, Box value) {
    // Format: (x1,y1),(x2,y2) — no surrounding brackets
    sb.append('(');
    sb.append(Double.toString(value.x1()));
    sb.append(',');
    sb.append(Double.toString(value.y1()));
    sb.append("),(");
    sb.append(Double.toString(value.x2()));
    sb.append(',');
    sb.append(Double.toString(value.y2()));
    sb.append(')');
  }

  @Override
  public Codec.ParsingResult<Box> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      // Format: (x1,y1),(x2,y2)
      // Box uses semicolon as separator in array context, but PG outputs comma-separated
      // Handle both comma and semicolon separators
      String sep = s.contains(";") ? ";" : null;
      int midSep;
      if (sep != null) {
        midSep = s.indexOf(';');
        Point p1 = PointCodec.parsePoint(s.substring(0, midSep).trim());
        Point p2 = PointCodec.parsePoint(s.substring(midSep + 1).trim());
        return new Codec.ParsingResult<>(new Box(p1.x(), p1.y(), p2.x(), p2.y()), input.length());
      }
      midSep = LsegCodec.findPointSeparator(s);
      Point p1 = PointCodec.parsePoint(s.substring(0, midSep));
      Point p2 = PointCodec.parsePoint(s.substring(midSep + 1));
      return new Codec.ParsingResult<>(new Box(p1.x(), p1.y(), p2.x(), p2.y()), input.length());
    } catch (Exception e) {
      throw new Codec.DecodingException(input, offset, "Invalid box: " + s);
    }
  }

  @Override
  public void encodeInBinary(Box value, ByteArrayOutputStream out) {
    PointCodec.writeFloat8(out, value.x1());
    PointCodec.writeFloat8(out, value.y1());
    PointCodec.writeFloat8(out, value.x2());
    PointCodec.writeFloat8(out, value.y2());
  }

  @Override
  public Box decodeInBinary(ByteBuffer buf, int length) {
    return new Box(buf.getDouble(), buf.getDouble(), buf.getDouble(), buf.getDouble());
  }

  @Override
  public Box random(Random r, int size) {
    if (size == 0) return new Box(0.0, 0.0, 0.0, 0.0);
    return Box.of(
        PointCodec.finiteDouble(r, size),
        PointCodec.finiteDouble(r, size),
        PointCodec.finiteDouble(r, size),
        PointCodec.finiteDouble(r, size));
  }
}
