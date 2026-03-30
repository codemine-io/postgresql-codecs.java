package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code lseg} values (line segment). */
final class LsegCodec implements Codec<Lseg> {

  @Override
  public String name() {
    return "lseg";
  }

  @Override
  public int scalarOid() {
    return 601;
  }

  @Override
  public int arrayOid() {
    return 1018;
  }

  @Override
  public void write(StringBuilder sb, Lseg value) {
    sb.append("[(");
    sb.append(Double.toString(value.x1()));
    sb.append(',');
    sb.append(Double.toString(value.y1()));
    sb.append("),(");
    sb.append(Double.toString(value.x2()));
    sb.append(',');
    sb.append(Double.toString(value.y2()));
    sb.append(")]");
  }

  @Override
  public Codec.ParsingResult<Lseg> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      // Format: [(x1,y1),(x2,y2)]
      if (s.startsWith("[")) s = s.substring(1);
      if (s.endsWith("]")) s = s.substring(0, s.length() - 1);
      s = s.trim();

      // Parse two points: (x1,y1),(x2,y2)
      int midComma = findPointSeparator(s);
      Point p1 = PointCodec.parsePoint(s.substring(0, midComma));
      Point p2 = PointCodec.parsePoint(s.substring(midComma + 1));

      return new Codec.ParsingResult<>(new Lseg(p1.x(), p1.y(), p2.x(), p2.y()), input.length());
    } catch (Exception e) {
      throw new Codec.DecodingException(input, offset, "Invalid lseg: " + s);
    }
  }

  @Override
  public void encodeInBinary(Lseg value, ByteArrayOutputStream out) {
    PointCodec.writeFloat8(out, value.x1());
    PointCodec.writeFloat8(out, value.y1());
    PointCodec.writeFloat8(out, value.x2());
    PointCodec.writeFloat8(out, value.y2());
  }

  @Override
  public Lseg decodeInBinary(ByteBuffer buf, int length) {
    return new Lseg(buf.getDouble(), buf.getDouble(), buf.getDouble(), buf.getDouble());
  }

  @Override
  public Lseg random(Random r, int size) {
    if (size == 0) return new Lseg(0.0, 0.0, 0.0, 0.0);
    return new Lseg(
        PointCodec.finiteDouble(r, size),
        PointCodec.finiteDouble(r, size),
        PointCodec.finiteDouble(r, size),
        PointCodec.finiteDouble(r, size));
  }

  /**
   * Finds the comma between two point literals: "(x1,y1),(x2,y2)". The separator is the comma that
   * appears between a closing and opening parenthesis: ),(
   */
  static int findPointSeparator(String s) {
    int depth = 0;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '(') depth++;
      else if (c == ')') depth--;
      else if (c == ',' && depth == 0) return i;
    }
    throw new IllegalArgumentException("No point separator found in: " + s);
  }
}
