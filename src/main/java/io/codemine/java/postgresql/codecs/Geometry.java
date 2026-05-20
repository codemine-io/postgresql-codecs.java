package io.codemine.java.postgresql.codecs;

import java.util.List;
import java.util.Objects;

/** PostGIS {@code geometry} payload modeled as a structured geometry tree. */
public sealed interface Geometry
    permits Geometry.Point,
        Geometry.Curve,
        Geometry.Surface,
        Geometry.MultiPoint,
        Geometry.MultiLineString,
        Geometry.MultiPolygon,
        Geometry.GeometryCollection,
        Geometry.MultiCurve,
        Geometry.MultiSurface,
        Geometry.Tin {

  /** Returns the declared coordinate dimension for this geometry tree. */
  CoordinateDimension coordinateDimension();

  /** Returns the geometry SRID, or {@code null} when it is absent. */
  Integer srid();

  /** Returns whether the geometry carries no coordinates. */
  boolean isEmpty();

  /** Coordinate dimension flags used by EWKB/EWKT. */
  enum CoordinateDimension {
    XY(false, false),
    XYZ(true, false),
    XYM(false, true),
    XYZM(true, true);

    private final boolean hasZ;
    private final boolean hasM;

    CoordinateDimension(boolean hasZ, boolean hasM) {
      this.hasZ = hasZ;
      this.hasM = hasM;
    }

    public boolean hasZ() {
      return hasZ;
    }

    public boolean hasM() {
      return hasM;
    }

    int ordinateCount() {
      return 2 + (hasZ ? 1 : 0) + (hasM ? 1 : 0);
    }

    static CoordinateDimension of(boolean hasZ, boolean hasM) {
      if (hasZ) {
        return hasM ? XYZM : XYZ;
      }
      return hasM ? XYM : XY;
    }

    void validate(Coordinate coordinate) {
      Objects.requireNonNull(coordinate, "coordinate");
      if (hasZ != (coordinate.z() != null) || hasM != (coordinate.m() != null)) {
        throw new IllegalArgumentException(
            "Coordinate "
                + coordinate
                + " does not match dimension "
                + this
                + " (hasZ="
                + hasZ
                + ", hasM="
                + hasM
                + ")");
      }
    }
  }

  /** Geometry coordinate with optional Z and M ordinates. */
  record Coordinate(double x, double y, Double z, Double m) {

    public static Coordinate xy(double x, double y) {
      return new Coordinate(x, y, null, null);
    }

    public static Coordinate xyz(double x, double y, double z) {
      return new Coordinate(x, y, z, null);
    }

    public static Coordinate xym(double x, double y, double m) {
      return new Coordinate(x, y, null, m);
    }

    public static Coordinate xyzm(double x, double y, double z, double m) {
      return new Coordinate(x, y, z, m);
    }
  }

  /** Marker for PostGIS curve subtypes. */
  sealed interface Curve extends Geometry permits LineString, CircularString, CompoundCurve {}

  /** Marker for PostGIS surface subtypes. */
  sealed interface Surface extends Geometry
      permits Polygon, CurvePolygon, PolyhedralSurface, Triangle {}

  /** PostGIS {@code POINT}. */
  record Point(CoordinateDimension coordinateDimension, Integer srid, Coordinate coordinate)
      implements Geometry {
    public Point {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      if (coordinate != null) {
        coordinateDimension.validate(coordinate);
      }
    }

    public static Point empty(CoordinateDimension coordinateDimension, Integer srid) {
      return new Point(coordinateDimension, srid, null);
    }

    @Override
    public boolean isEmpty() {
      return coordinate == null;
    }
  }

  /** PostGIS {@code LINESTRING}. */
  record LineString(CoordinateDimension coordinateDimension, Integer srid, List<Coordinate> points)
      implements Curve {
    public LineString {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      points = copyCoordinates(points, coordinateDimension, "points");
    }

    @Override
    public boolean isEmpty() {
      return points.isEmpty();
    }
  }

  /** PostGIS {@code POLYGON}. */
  record Polygon(
      CoordinateDimension coordinateDimension, Integer srid, List<List<Coordinate>> rings)
      implements Surface {
    public Polygon {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      rings = copyCoordinateRings(rings, coordinateDimension, "rings");
    }

    @Override
    public boolean isEmpty() {
      return rings.isEmpty();
    }
  }

  /** PostGIS {@code MULTIPOINT}. */
  record MultiPoint(CoordinateDimension coordinateDimension, Integer srid, List<Point> points)
      implements Geometry {
    public MultiPoint {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      points = copyList(points, "points");
    }

    @Override
    public boolean isEmpty() {
      return points.isEmpty();
    }
  }

  /** PostGIS {@code MULTILINESTRING}. */
  record MultiLineString(
      CoordinateDimension coordinateDimension, Integer srid, List<LineString> lineStrings)
      implements Geometry {
    public MultiLineString {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      lineStrings = copyList(lineStrings, "lineStrings");
    }

    @Override
    public boolean isEmpty() {
      return lineStrings.isEmpty();
    }
  }

  /** PostGIS {@code MULTIPOLYGON}. */
  record MultiPolygon(CoordinateDimension coordinateDimension, Integer srid, List<Polygon> polygons)
      implements Geometry {
    public MultiPolygon {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      polygons = copyList(polygons, "polygons");
    }

    @Override
    public boolean isEmpty() {
      return polygons.isEmpty();
    }
  }

  /** PostGIS {@code GEOMETRYCOLLECTION}. */
  record GeometryCollection(
      CoordinateDimension coordinateDimension, Integer srid, List<Geometry> geometries)
      implements Geometry {
    public GeometryCollection {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      geometries = copyList(geometries, "geometries");
    }

    @Override
    public boolean isEmpty() {
      return geometries.isEmpty();
    }
  }

  /** PostGIS {@code CIRCULARSTRING}. */
  record CircularString(
      CoordinateDimension coordinateDimension, Integer srid, List<Coordinate> points)
      implements Curve {
    public CircularString {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      points = copyCoordinates(points, coordinateDimension, "points");
    }

    @Override
    public boolean isEmpty() {
      return points.isEmpty();
    }
  }

  /** PostGIS {@code COMPOUNDCURVE}. */
  record CompoundCurve(CoordinateDimension coordinateDimension, Integer srid, List<Curve> curves)
      implements Curve {
    public CompoundCurve {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      curves = copyList(curves, "curves");
    }

    @Override
    public boolean isEmpty() {
      return curves.isEmpty();
    }
  }

  /** PostGIS {@code CURVEPOLYGON}. */
  record CurvePolygon(CoordinateDimension coordinateDimension, Integer srid, List<Curve> rings)
      implements Surface {
    public CurvePolygon {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      rings = copyList(rings, "rings");
    }

    @Override
    public boolean isEmpty() {
      return rings.isEmpty();
    }
  }

  /** PostGIS {@code MULTICURVE}. */
  record MultiCurve(CoordinateDimension coordinateDimension, Integer srid, List<Curve> curves)
      implements Geometry {
    public MultiCurve {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      curves = copyList(curves, "curves");
    }

    @Override
    public boolean isEmpty() {
      return curves.isEmpty();
    }
  }

  /** PostGIS {@code MULTISURFACE}. */
  record MultiSurface(CoordinateDimension coordinateDimension, Integer srid, List<Surface> surfaces)
      implements Geometry {
    public MultiSurface {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      surfaces = copyList(surfaces, "surfaces");
    }

    @Override
    public boolean isEmpty() {
      return surfaces.isEmpty();
    }
  }

  /** PostGIS {@code POLYHEDRALSURFACE}. */
  record PolyhedralSurface(
      CoordinateDimension coordinateDimension, Integer srid, List<Polygon> patches)
      implements Surface {
    public PolyhedralSurface {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      patches = copyList(patches, "patches");
    }

    @Override
    public boolean isEmpty() {
      return patches.isEmpty();
    }
  }

  /** PostGIS {@code TRIANGLE}. */
  record Triangle(CoordinateDimension coordinateDimension, Integer srid, List<Coordinate> ring)
      implements Surface {
    public Triangle {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      ring = copyCoordinates(ring, coordinateDimension, "ring");
    }

    @Override
    public boolean isEmpty() {
      return ring.isEmpty();
    }
  }

  /** PostGIS {@code TIN}. */
  record Tin(CoordinateDimension coordinateDimension, Integer srid, List<Triangle> triangles)
      implements Geometry {
    public Tin {
      Objects.requireNonNull(coordinateDimension, "coordinateDimension");
      triangles = copyList(triangles, "triangles");
    }

    @Override
    public boolean isEmpty() {
      return triangles.isEmpty();
    }
  }

  private static List<Coordinate> copyCoordinates(
      List<Coordinate> coordinates, CoordinateDimension coordinateDimension, String fieldName) {
    List<Coordinate> copy = copyList(coordinates, fieldName);
    for (Coordinate coordinate : copy) {
      coordinateDimension.validate(coordinate);
    }
    return copy;
  }

  private static List<List<Coordinate>> copyCoordinateRings(
      List<List<Coordinate>> rings, CoordinateDimension coordinateDimension, String fieldName) {
    List<List<Coordinate>> copy = copyList(rings, fieldName);
    return copy.stream()
        .map(ring -> copyCoordinates(ring, coordinateDimension, fieldName + "[]"))
        .toList();
  }

  private static <A> List<A> copyList(List<A> values, String fieldName) {
    Objects.requireNonNull(values, fieldName);
    List<A> copy = List.copyOf(values);
    for (A value : copy) {
      Objects.requireNonNull(value, fieldName + " element");
    }
    return copy;
  }
}
