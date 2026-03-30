package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Codec for PostgreSQL {@code polygon} values. */
final class PolygonCodec implements Codec<Polygon> {

  @Override
  public String name() {
    return "polygon";
  }

  @Override
  public int scalarOid() {
    return 604;
  }

  @Override
  public int arrayOid() {
    return 1027;
  }

  @Override
  public void write(StringBuilder sb, Polygon value) {
    sb.append('(');
    for (int i = 0; i < value.points().size(); i++) {
      if (i > 0) sb.append(',');
      Point p = value.points().get(i);
      sb.append('(');
      sb.append(Double.toString(p.x()));
      sb.append(',');
      sb.append(Double.toString(p.y()));
      sb.append(')');
    }
    sb.append(')');
  }

  @Override
  public Codec.ParsingResult<Polygon> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    try {
      // Format: ((x1,y1),(x2,y2),...)
      // Strip outer parens
      if (s.startsWith("(") && s.endsWith(")")) {
        s = s.substring(1, s.length() - 1);
      }
      List<Point> points = PathCodec.parsePoints(s);
      return new Codec.ParsingResult<>(new Polygon(points), input.length());
    } catch (Exception e) {
      throw new Codec.DecodingException(input, offset, "Invalid polygon: " + s);
    }
  }

  @Override
  public void encodeInBinary(Polygon value, ByteArrayOutputStream out) {
    int numPoints = value.points().size();
    out.write((numPoints >>> 24) & 0xFF);
    out.write((numPoints >>> 16) & 0xFF);
    out.write((numPoints >>> 8) & 0xFF);
    out.write(numPoints & 0xFF);
    for (Point p : value.points()) {
      PointCodec.writeFloat8(out, p.x());
      PointCodec.writeFloat8(out, p.y());
    }
  }

  @Override
  public Polygon decodeInBinary(ByteBuffer buf, int length) {
    int numPoints = buf.getInt();
    List<Point> points = new ArrayList<>(numPoints);
    for (int i = 0; i < numPoints; i++) {
      points.add(new Point(buf.getDouble(), buf.getDouble()));
    }
    return new Polygon(points);
  }

  @Override
  public Polygon random(Random r, int size) {
    int numPoints = size == 0 ? 1 : r.nextInt(1, Math.min(size, 10) + 1);
    List<Point> points = new ArrayList<>(numPoints);
    for (int i = 0; i < numPoints; i++) {
      points.add(new Point(PointCodec.finiteDouble(r, size), PointCodec.finiteDouble(r, size)));
    }
    return new Polygon(points);
  }
}
