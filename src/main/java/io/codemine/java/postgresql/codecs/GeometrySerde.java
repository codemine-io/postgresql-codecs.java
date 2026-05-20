package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class GeometrySerde {
  private static final int FLAG_Z = 0x80000000;
  private static final int FLAG_M = 0x40000000;
  private static final int FLAG_SRID = 0x20000000;
  private static final int TYPE_MASK = 0x1FFFFFFF;

  private static final int TYPE_POINT = 1;
  private static final int TYPE_LINESTRING = 2;
  private static final int TYPE_POLYGON = 3;
  private static final int TYPE_MULTI_POINT = 4;
  private static final int TYPE_MULTI_LINESTRING = 5;
  private static final int TYPE_MULTI_POLYGON = 6;
  private static final int TYPE_GEOMETRY_COLLECTION = 7;
  private static final int TYPE_CIRCULAR_STRING = 8;
  private static final int TYPE_COMPOUND_CURVE = 9;
  private static final int TYPE_CURVE_POLYGON = 10;
  private static final int TYPE_MULTI_CURVE = 11;
  private static final int TYPE_MULTI_SURFACE = 12;
  private static final int TYPE_POLYHEDRAL_SURFACE = 15;
  private static final int TYPE_TIN = 16;
  private static final int TYPE_TRIANGLE = 17;

  private GeometrySerde() {}

  static byte[] encodeGeometry(Geometry geometry) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writeGeometry(out, geometry);
    return out.toByteArray();
  }

  static Geometry decodeGeometry(ByteBuffer buffer, int length) throws Codec.DecodingException {
    ByteBuffer slice = buffer.slice();
    slice.limit(length);
    Geometry geometry = readGeometry(slice);
    buffer.position(buffer.position() + length);
    return geometry;
  }

  static String encodeHex(byte[] bytes) {
    char[] chars = new char[bytes.length * 2];
    char[] alphabet = "0123456789abcdef".toCharArray();
    for (int i = 0; i < bytes.length; i++) {
      int value = bytes[i] & 0xFF;
      chars[i * 2] = alphabet[value >>> 4];
      chars[i * 2 + 1] = alphabet[value & 0x0F];
    }
    return new String(chars);
  }

  static byte[] decodeHex(CharSequence input) throws Codec.DecodingException {
    String text = input.toString().trim();
    if (text.startsWith("\\x") || text.startsWith("\\X")) {
      text = text.substring(2);
    }
    if ((text.length() & 1) != 0) {
      throw new Codec.DecodingException(input, 0, "EWKB hex payload must have even length");
    }
    byte[] bytes = new byte[text.length() / 2];
    for (int i = 0; i < text.length(); i += 2) {
      int hi = Character.digit(text.charAt(i), 16);
      int lo = Character.digit(text.charAt(i + 1), 16);
      if (hi < 0 || lo < 0) {
        throw new Codec.DecodingException(input, i, "Invalid EWKB hex digit");
      }
      bytes[i / 2] = (byte) ((hi << 4) | lo);
    }
    return bytes;
  }

  static Geometry randomGeometry(Random random, int size) {
    int choice = random.nextInt(16);
    return switch (choice) {
      case 0 -> randomPoint(random, size);
      case 1 -> randomLineString(random, size);
      case 2 -> randomPolygon(random, size);
      case 3 -> randomMultiPoint(random, size);
      case 4 -> randomMultiLineString(random, size);
      case 5 -> randomMultiPolygon(random, size);
      case 6 -> randomGeometryCollection(random, size);
      case 7 -> fixedCircularString();
      case 8 -> fixedCompoundCurve();
      case 9 -> fixedCurvePolygon();
      case 10 -> fixedMultiCurve();
      case 11 -> fixedMultiSurface();
      case 12 -> fixedPolyhedralSurface();
      case 13 -> fixedTriangle();
      case 14 -> fixedTin();
      default -> randomMeasuredPoint(random, size);
    };
  }

  static Geography randomGeography(Random random, int size) {
    Geometry.Point point =
        new Geometry.Point(
            Geometry.CoordinateDimension.XY,
            4326,
            Geometry.Coordinate.xy(longitude(random), latitude(random)));
    Geometry.LineString line =
        new Geometry.LineString(
            Geometry.CoordinateDimension.XY,
            4326,
            List.of(
                Geometry.Coordinate.xy(longitude(random), latitude(random)),
                Geometry.Coordinate.xy(longitude(random), latitude(random))));
    return new Geography(random.nextBoolean() ? point : line);
  }

  private static Geometry readGeometry(ByteBuffer buffer) throws Codec.DecodingException {
    ensureRemaining(buffer, 5, "geometry header");
    byte endianMarker = buffer.get();
    ByteOrder order =
        switch (endianMarker) {
          case 0 -> ByteOrder.BIG_ENDIAN;
          case 1 -> ByteOrder.LITTLE_ENDIAN;
          default ->
              throw new Codec.DecodingException("Unsupported EWKB endian marker: " + endianMarker);
        };
    int typeWord = readInt(buffer, order);
    boolean hasZ = (typeWord & FLAG_Z) != 0;
    boolean hasM = (typeWord & FLAG_M) != 0;
    boolean hasSrid = (typeWord & FLAG_SRID) != 0;
    Geometry.CoordinateDimension dimension = Geometry.CoordinateDimension.of(hasZ, hasM);
    Integer srid = hasSrid ? readInt(buffer, order) : null;
    int type = typeWord & TYPE_MASK;
    return switch (type) {
      case TYPE_POINT -> readPoint(buffer, order, dimension, srid);
      case TYPE_LINESTRING ->
          new Geometry.LineString(
              dimension, srid, readCoordinateSequence(buffer, order, dimension));
      case TYPE_POLYGON ->
          new Geometry.Polygon(dimension, srid, readCoordinateRings(buffer, order, dimension));
      case TYPE_MULTI_POINT ->
          new Geometry.MultiPoint(
              dimension,
              srid,
              readTypedGeometries(buffer, order, Geometry.Point.class, "MultiPoint"));
      case TYPE_MULTI_LINESTRING ->
          new Geometry.MultiLineString(
              dimension,
              srid,
              readTypedGeometries(buffer, order, Geometry.LineString.class, "MultiLineString"));
      case TYPE_MULTI_POLYGON ->
          new Geometry.MultiPolygon(
              dimension,
              srid,
              readTypedGeometries(buffer, order, Geometry.Polygon.class, "MultiPolygon"));
      case TYPE_GEOMETRY_COLLECTION ->
          new Geometry.GeometryCollection(dimension, srid, readGeometries(buffer, order));
      case TYPE_CIRCULAR_STRING ->
          new Geometry.CircularString(
              dimension, srid, readCoordinateSequence(buffer, order, dimension));
      case TYPE_COMPOUND_CURVE ->
          new Geometry.CompoundCurve(
              dimension,
              srid,
              readTypedGeometries(buffer, order, Geometry.Curve.class, "CompoundCurve"));
      case TYPE_CURVE_POLYGON ->
          new Geometry.CurvePolygon(
              dimension,
              srid,
              readTypedGeometries(buffer, order, Geometry.Curve.class, "CurvePolygon"));
      case TYPE_MULTI_CURVE ->
          new Geometry.MultiCurve(
              dimension,
              srid,
              readTypedGeometries(buffer, order, Geometry.Curve.class, "MultiCurve"));
      case TYPE_MULTI_SURFACE ->
          new Geometry.MultiSurface(
              dimension,
              srid,
              readTypedGeometries(buffer, order, Geometry.Surface.class, "MultiSurface"));
      case TYPE_POLYHEDRAL_SURFACE ->
          new Geometry.PolyhedralSurface(
              dimension,
              srid,
              readTypedGeometries(buffer, order, Geometry.Polygon.class, "PolyhedralSurface"));
      case TYPE_TIN ->
          new Geometry.Tin(
              dimension, srid, readTypedGeometries(buffer, order, Geometry.Triangle.class, "Tin"));
      case TYPE_TRIANGLE -> readTriangle(buffer, order, dimension, srid);
      default -> throw new Codec.DecodingException("Unsupported EWKB geometry type: " + type);
    };
  }

  private static Geometry.Point readPoint(
      ByteBuffer buffer, ByteOrder order, Geometry.CoordinateDimension dimension, Integer srid)
      throws Codec.DecodingException {
    Geometry.Coordinate coordinate = readPointCoordinate(buffer, order, dimension);
    if (coordinate == null) {
      return Geometry.Point.empty(dimension, srid);
    }
    return new Geometry.Point(dimension, srid, coordinate);
  }

  private static Geometry.Triangle readTriangle(
      ByteBuffer buffer, ByteOrder order, Geometry.CoordinateDimension dimension, Integer srid)
      throws Codec.DecodingException {
    List<List<Geometry.Coordinate>> rings = readCoordinateRings(buffer, order, dimension);
    if (rings.size() != 1) {
      throw new Codec.DecodingException("Triangle EWKB must contain exactly one ring");
    }
    return new Geometry.Triangle(dimension, srid, rings.getFirst());
  }

  private static List<Geometry> readGeometries(ByteBuffer buffer, ByteOrder order)
      throws Codec.DecodingException {
    int count = readInt(buffer, order);
    List<Geometry> values = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      values.add(readGeometry(buffer));
    }
    return values;
  }

  private static <A> List<A> readTypedGeometries(
      ByteBuffer buffer, ByteOrder order, Class<A> expectedType, String owner)
      throws Codec.DecodingException {
    int count = readInt(buffer, order);
    List<A> values = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      Geometry geometry = readGeometry(buffer);
      if (!expectedType.isInstance(geometry)) {
        throw new Codec.DecodingException(
            owner
                + " expected members of type "
                + expectedType.getSimpleName()
                + " but found "
                + geometry.getClass().getSimpleName());
      }
      values.add(expectedType.cast(geometry));
    }
    return values;
  }

  private static List<List<Geometry.Coordinate>> readCoordinateRings(
      ByteBuffer buffer, ByteOrder order, Geometry.CoordinateDimension dimension)
      throws Codec.DecodingException {
    int count = readInt(buffer, order);
    List<List<Geometry.Coordinate>> rings = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      rings.add(readCoordinateSequence(buffer, order, dimension));
    }
    return rings;
  }

  private static List<Geometry.Coordinate> readCoordinateSequence(
      ByteBuffer buffer, ByteOrder order, Geometry.CoordinateDimension dimension)
      throws Codec.DecodingException {
    int count = readInt(buffer, order);
    List<Geometry.Coordinate> points = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      points.add(readCoordinate(buffer, order, dimension));
    }
    return points;
  }

  private static Geometry.Coordinate readPointCoordinate(
      ByteBuffer buffer, ByteOrder order, Geometry.CoordinateDimension dimension)
      throws Codec.DecodingException {
    Geometry.Coordinate coordinate = readCoordinate(buffer, order, dimension);
    boolean empty =
        Double.isNaN(coordinate.x())
            && Double.isNaN(coordinate.y())
            && (!dimension.hasZ() || Double.isNaN(coordinate.z()))
            && (!dimension.hasM() || Double.isNaN(coordinate.m()));
    return empty ? null : coordinate;
  }

  private static Geometry.Coordinate readCoordinate(
      ByteBuffer buffer, ByteOrder order, Geometry.CoordinateDimension dimension)
      throws Codec.DecodingException {
    ensureRemaining(buffer, dimension.ordinateCount() * Double.BYTES, "coordinate");
    double x = readDouble(buffer, order);
    double y = readDouble(buffer, order);
    Double z = dimension.hasZ() ? readDouble(buffer, order) : null;
    Double m = dimension.hasM() ? readDouble(buffer, order) : null;
    return new Geometry.Coordinate(x, y, z, m);
  }

  private static void writeGeometry(ByteArrayOutputStream out, Geometry geometry) {
    Geometry.CoordinateDimension dimension = geometry.coordinateDimension();
    int typeWord =
        typeCode(geometry)
            | (dimension.hasZ() ? FLAG_Z : 0)
            | (dimension.hasM() ? FLAG_M : 0)
            | (geometry.srid() != null ? FLAG_SRID : 0);
    out.write(1);
    writeInt(out, typeWord);
    if (geometry.srid() != null) {
      writeInt(out, geometry.srid());
    }
    switch (geometry) {
      case Geometry.Point point -> writePoint(out, point);
      case Geometry.LineString lineString ->
          writeCoordinateSequence(out, lineString.points(), lineString.coordinateDimension());
      case Geometry.Polygon polygon ->
          writeCoordinateRings(out, polygon.rings(), polygon.coordinateDimension());
      case Geometry.MultiPoint multiPoint -> writeNestedGeometries(out, multiPoint.points());
      case Geometry.MultiLineString multiLineString ->
          writeNestedGeometries(out, multiLineString.lineStrings());
      case Geometry.MultiPolygon multiPolygon ->
          writeNestedGeometries(out, multiPolygon.polygons());
      case Geometry.GeometryCollection geometryCollection ->
          writeNestedGeometries(out, geometryCollection.geometries());
      case Geometry.CircularString circularString ->
          writeCoordinateSequence(
              out, circularString.points(), circularString.coordinateDimension());
      case Geometry.CompoundCurve compoundCurve ->
          writeNestedGeometries(out, compoundCurve.curves());
      case Geometry.CurvePolygon curvePolygon -> writeNestedGeometries(out, curvePolygon.rings());
      case Geometry.MultiCurve multiCurve -> writeNestedGeometries(out, multiCurve.curves());
      case Geometry.MultiSurface multiSurface ->
          writeNestedGeometries(out, multiSurface.surfaces());
      case Geometry.PolyhedralSurface polyhedralSurface ->
          writeNestedGeometries(out, polyhedralSurface.patches());
      case Geometry.Triangle triangle ->
          writeCoordinateRings(out, List.of(triangle.ring()), triangle.coordinateDimension());
      case Geometry.Tin tin -> writeNestedGeometries(out, tin.triangles());
    }
  }

  private static void writePoint(ByteArrayOutputStream out, Geometry.Point point) {
    Geometry.CoordinateDimension dimension = point.coordinateDimension();
    if (point.isEmpty()) {
      writeCoordinate(
          out,
          new Geometry.Coordinate(
              Double.NaN,
              Double.NaN,
              dimension.hasZ() ? Double.NaN : null,
              dimension.hasM() ? Double.NaN : null),
          dimension);
    } else {
      writeCoordinate(out, point.coordinate(), dimension);
    }
  }

  private static void writeNestedGeometries(
      ByteArrayOutputStream out, List<? extends Geometry> values) {
    writeInt(out, values.size());
    for (Geometry value : values) {
      writeGeometry(out, value);
    }
  }

  private static void writeCoordinateRings(
      ByteArrayOutputStream out,
      List<List<Geometry.Coordinate>> rings,
      Geometry.CoordinateDimension dimension) {
    writeInt(out, rings.size());
    for (List<Geometry.Coordinate> ring : rings) {
      writeCoordinateSequence(out, ring, dimension);
    }
  }

  private static void writeCoordinateSequence(
      ByteArrayOutputStream out,
      List<Geometry.Coordinate> coordinates,
      Geometry.CoordinateDimension dimension) {
    writeInt(out, coordinates.size());
    for (Geometry.Coordinate coordinate : coordinates) {
      writeCoordinate(out, coordinate, dimension);
    }
  }

  private static void writeCoordinate(
      ByteArrayOutputStream out,
      Geometry.Coordinate coordinate,
      Geometry.CoordinateDimension dimension) {
    writeDouble(out, coordinate.x());
    writeDouble(out, coordinate.y());
    if (dimension.hasZ()) {
      writeDouble(out, coordinate.z());
    }
    if (dimension.hasM()) {
      writeDouble(out, coordinate.m());
    }
  }

  private static int typeCode(Geometry geometry) {
    return switch (geometry) {
      case Geometry.Point ignored -> TYPE_POINT;
      case Geometry.LineString ignored -> TYPE_LINESTRING;
      case Geometry.Polygon ignored -> TYPE_POLYGON;
      case Geometry.MultiPoint ignored -> TYPE_MULTI_POINT;
      case Geometry.MultiLineString ignored -> TYPE_MULTI_LINESTRING;
      case Geometry.MultiPolygon ignored -> TYPE_MULTI_POLYGON;
      case Geometry.GeometryCollection ignored -> TYPE_GEOMETRY_COLLECTION;
      case Geometry.CircularString ignored -> TYPE_CIRCULAR_STRING;
      case Geometry.CompoundCurve ignored -> TYPE_COMPOUND_CURVE;
      case Geometry.CurvePolygon ignored -> TYPE_CURVE_POLYGON;
      case Geometry.MultiCurve ignored -> TYPE_MULTI_CURVE;
      case Geometry.MultiSurface ignored -> TYPE_MULTI_SURFACE;
      case Geometry.PolyhedralSurface ignored -> TYPE_POLYHEDRAL_SURFACE;
      case Geometry.Triangle ignored -> TYPE_TRIANGLE;
      case Geometry.Tin ignored -> TYPE_TIN;
    };
  }

  private static int readInt(ByteBuffer buffer, ByteOrder order) {
    return order == ByteOrder.LITTLE_ENDIAN
        ? Integer.reverseBytes(buffer.getInt())
        : buffer.getInt();
  }

  private static double readDouble(ByteBuffer buffer, ByteOrder order) {
    long bits =
        order == ByteOrder.LITTLE_ENDIAN ? Long.reverseBytes(buffer.getLong()) : buffer.getLong();
    return Double.longBitsToDouble(bits);
  }

  private static void writeInt(ByteArrayOutputStream out, int value) {
    out.write(value & 0xFF);
    out.write((value >>> 8) & 0xFF);
    out.write((value >>> 16) & 0xFF);
    out.write((value >>> 24) & 0xFF);
  }

  private static void writeDouble(ByteArrayOutputStream out, double value) {
    long bits = Double.doubleToLongBits(value);
    out.write((int) bits & 0xFF);
    out.write((int) (bits >>> 8) & 0xFF);
    out.write((int) (bits >>> 16) & 0xFF);
    out.write((int) (bits >>> 24) & 0xFF);
    out.write((int) (bits >>> 32) & 0xFF);
    out.write((int) (bits >>> 40) & 0xFF);
    out.write((int) (bits >>> 48) & 0xFF);
    out.write((int) (bits >>> 56) & 0xFF);
  }

  private static void ensureRemaining(ByteBuffer buffer, int expected, String label)
      throws Codec.DecodingException {
    if (buffer.remaining() < expected) {
      throw new Codec.DecodingException(
          "Unexpected end of EWKB while reading " + label + ": need " + expected + " bytes");
    }
  }

  private static double value(Random random, int size) {
    double scale = Math.max(size, 1);
    return (random.nextDouble() * 2.0 - 1.0) * scale;
  }

  private static Geometry.Point randomPoint(Random random, int size) {
    return new Geometry.Point(
        Geometry.CoordinateDimension.XY,
        random.nextBoolean() ? 4326 : null,
        Geometry.Coordinate.xy(value(random, size), value(random, size)));
  }

  private static Geometry.Point randomMeasuredPoint(Random random, int size) {
    return switch (random.nextInt(3)) {
      case 0 ->
          new Geometry.Point(
              Geometry.CoordinateDimension.XYM,
              random.nextBoolean() ? 4326 : null,
              Geometry.Coordinate.xym(
                  value(random, size), value(random, size), value(random, size)));
      case 1 ->
          new Geometry.Point(
              Geometry.CoordinateDimension.XYZ,
              random.nextBoolean() ? 3857 : null,
              Geometry.Coordinate.xyz(
                  value(random, size), value(random, size), value(random, size)));
      default ->
          new Geometry.Point(
              Geometry.CoordinateDimension.XYZM,
              random.nextBoolean() ? 4326 : null,
              Geometry.Coordinate.xyzm(
                  value(random, size),
                  value(random, size),
                  value(random, size),
                  value(random, size)));
    };
  }

  private static Geometry.LineString randomLineString(Random random, int size) {
    return new Geometry.LineString(
        Geometry.CoordinateDimension.XY,
        random.nextBoolean() ? 4326 : null,
        List.of(
            Geometry.Coordinate.xy(value(random, size), value(random, size)),
            Geometry.Coordinate.xy(value(random, size), value(random, size)),
            Geometry.Coordinate.xy(value(random, size), value(random, size))));
  }

  private static Geometry.Polygon randomPolygon(Random random, int size) {
    double x = value(random, size);
    double y = value(random, size);
    double width = Math.max(random.nextDouble() * Math.max(size, 1), 0.25d);
    double height = Math.max(random.nextDouble() * Math.max(size, 1), 0.25d);
    return new Geometry.Polygon(
        Geometry.CoordinateDimension.XY,
        random.nextBoolean() ? 4326 : null,
        List.of(
            List.of(
                Geometry.Coordinate.xy(x, y),
                Geometry.Coordinate.xy(x + width, y),
                Geometry.Coordinate.xy(x + width, y + height),
                Geometry.Coordinate.xy(x, y + height),
                Geometry.Coordinate.xy(x, y))));
  }

  private static Geometry.MultiPoint randomMultiPoint(Random random, int size) {
    return new Geometry.MultiPoint(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(randomPointWithoutSrid(random, size), randomPointWithoutSrid(random, size)));
  }

  private static Geometry.MultiLineString randomMultiLineString(Random random, int size) {
    return new Geometry.MultiLineString(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(
            randomLineStringWithoutSrid(random, size), randomLineStringWithoutSrid(random, size)));
  }

  private static Geometry.MultiPolygon randomMultiPolygon(Random random, int size) {
    return new Geometry.MultiPolygon(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(randomPolygonWithoutSrid(random, size), randomPolygonWithoutSrid(random, size)));
  }

  private static Geometry.GeometryCollection randomGeometryCollection(Random random, int size) {
    return new Geometry.GeometryCollection(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(randomPointWithoutSrid(random, size), randomLineStringWithoutSrid(random, size)));
  }

  private static Geometry.Point randomPointWithoutSrid(Random random, int size) {
    return new Geometry.Point(
        Geometry.CoordinateDimension.XY,
        null,
        Geometry.Coordinate.xy(value(random, size), value(random, size)));
  }

  private static Geometry.LineString randomLineStringWithoutSrid(Random random, int size) {
    return new Geometry.LineString(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(
            Geometry.Coordinate.xy(value(random, size), value(random, size)),
            Geometry.Coordinate.xy(value(random, size), value(random, size)),
            Geometry.Coordinate.xy(value(random, size), value(random, size))));
  }

  private static Geometry.Polygon randomPolygonWithoutSrid(Random random, int size) {
    double x = value(random, size);
    double y = value(random, size);
    double width = Math.max(random.nextDouble() * Math.max(size, 1), 0.25d);
    double height = Math.max(random.nextDouble() * Math.max(size, 1), 0.25d);
    return new Geometry.Polygon(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(
            List.of(
                Geometry.Coordinate.xy(x, y),
                Geometry.Coordinate.xy(x + width, y),
                Geometry.Coordinate.xy(x + width, y + height),
                Geometry.Coordinate.xy(x, y + height),
                Geometry.Coordinate.xy(x, y))));
  }

  private static Geometry.CircularString fixedCircularString() {
    return new Geometry.CircularString(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(
            Geometry.Coordinate.xy(0, 0),
            Geometry.Coordinate.xy(1, 1),
            Geometry.Coordinate.xy(1, 0)));
  }

  private static Geometry.CompoundCurve fixedCompoundCurve() {
    return new Geometry.CompoundCurve(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(
            fixedCircularString(),
            new Geometry.LineString(
                Geometry.CoordinateDimension.XY,
                null,
                List.of(Geometry.Coordinate.xy(1, 0), Geometry.Coordinate.xy(0, 1)))));
  }

  private static Geometry.CurvePolygon fixedCurvePolygon() {
    return new Geometry.CurvePolygon(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(
            new Geometry.CircularString(
                Geometry.CoordinateDimension.XY,
                null,
                List.of(
                    Geometry.Coordinate.xy(0, 0),
                    Geometry.Coordinate.xy(4, 0),
                    Geometry.Coordinate.xy(4, 4),
                    Geometry.Coordinate.xy(0, 4),
                    Geometry.Coordinate.xy(0, 0))),
            new Geometry.LineString(
                Geometry.CoordinateDimension.XY,
                null,
                List.of(
                    Geometry.Coordinate.xy(1, 1),
                    Geometry.Coordinate.xy(3, 3),
                    Geometry.Coordinate.xy(3, 1),
                    Geometry.Coordinate.xy(1, 1)))));
  }

  private static Geometry.MultiCurve fixedMultiCurve() {
    return new Geometry.MultiCurve(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(
            new Geometry.LineString(
                Geometry.CoordinateDimension.XY,
                null,
                List.of(Geometry.Coordinate.xy(0, 0), Geometry.Coordinate.xy(5, 5))),
            new Geometry.CircularString(
                Geometry.CoordinateDimension.XY,
                null,
                List.of(
                    Geometry.Coordinate.xy(4, 0),
                    Geometry.Coordinate.xy(4, 4),
                    Geometry.Coordinate.xy(8, 4)))));
  }

  private static Geometry.MultiSurface fixedMultiSurface() {
    return new Geometry.MultiSurface(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(
            fixedCurvePolygon(),
            new Geometry.Polygon(
                Geometry.CoordinateDimension.XY,
                null,
                List.of(
                    List.of(
                        Geometry.Coordinate.xy(10, 10),
                        Geometry.Coordinate.xy(14, 12),
                        Geometry.Coordinate.xy(11, 10),
                        Geometry.Coordinate.xy(10, 10)),
                    List.of(
                        Geometry.Coordinate.xy(11, 11),
                        Geometry.Coordinate.xy(11.5, 11),
                        Geometry.Coordinate.xy(11, 11.5),
                        Geometry.Coordinate.xy(11, 11))))));
  }

  private static Geometry.PolyhedralSurface fixedPolyhedralSurface() {
    return new Geometry.PolyhedralSurface(
        Geometry.CoordinateDimension.XYZ,
        null,
        List.of(
            new Geometry.Polygon(
                Geometry.CoordinateDimension.XYZ,
                null,
                List.of(
                    List.of(
                        Geometry.Coordinate.xyz(0, 0, 0),
                        Geometry.Coordinate.xyz(0, 0, 1),
                        Geometry.Coordinate.xyz(0, 1, 1),
                        Geometry.Coordinate.xyz(0, 1, 0),
                        Geometry.Coordinate.xyz(0, 0, 0)))),
            new Geometry.Polygon(
                Geometry.CoordinateDimension.XYZ,
                null,
                List.of(
                    List.of(
                        Geometry.Coordinate.xyz(0, 0, 0),
                        Geometry.Coordinate.xyz(0, 1, 0),
                        Geometry.Coordinate.xyz(1, 1, 0),
                        Geometry.Coordinate.xyz(1, 0, 0),
                        Geometry.Coordinate.xyz(0, 0, 0))))));
  }

  private static Geometry.Triangle fixedTriangle() {
    return new Geometry.Triangle(
        Geometry.CoordinateDimension.XY,
        null,
        List.of(
            Geometry.Coordinate.xy(0, 0),
            Geometry.Coordinate.xy(0, 9),
            Geometry.Coordinate.xy(9, 0),
            Geometry.Coordinate.xy(0, 0)));
  }

  private static Geometry.Tin fixedTin() {
    return new Geometry.Tin(
        Geometry.CoordinateDimension.XYZ,
        null,
        List.of(
            new Geometry.Triangle(
                Geometry.CoordinateDimension.XYZ,
                null,
                List.of(
                    Geometry.Coordinate.xyz(0, 0, 0),
                    Geometry.Coordinate.xyz(0, 0, 1),
                    Geometry.Coordinate.xyz(0, 1, 0),
                    Geometry.Coordinate.xyz(0, 0, 0))),
            new Geometry.Triangle(
                Geometry.CoordinateDimension.XYZ,
                null,
                List.of(
                    Geometry.Coordinate.xyz(0, 0, 0),
                    Geometry.Coordinate.xyz(0, 1, 0),
                    Geometry.Coordinate.xyz(1, 1, 0),
                    Geometry.Coordinate.xyz(0, 0, 0)))));
  }

  private static double longitude(Random random) {
    return (random.nextDouble() * 360.0) - 180.0;
  }

  private static double latitude(Random random) {
    return (random.nextDouble() * 180.0) - 90.0;
  }
}
