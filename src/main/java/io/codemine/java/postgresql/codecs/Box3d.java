package io.codemine.java.postgresql.codecs;

/** PostGIS {@code box3d} type. */
public record Box3d(double xmin, double ymin, double zmin, double xmax, double ymax, double zmax) {

  /** Builds a canonical box from any two opposite corners. */
  public static Box3d of(double x1, double y1, double z1, double x2, double y2, double z2) {
    return new Box3d(
        Math.min(x1, x2),
        Math.min(y1, y2),
        Math.min(z1, z2),
        Math.max(x1, x2),
        Math.max(y1, y2),
        Math.max(z1, z2));
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendInTextTo(sb);
    return sb.toString();
  }

  void appendInTextTo(StringBuilder sb) {
    sb.append("BOX3D(")
        .append(xmin)
        .append(' ')
        .append(ymin)
        .append(' ')
        .append(zmin)
        .append(',')
        .append(xmax)
        .append(' ')
        .append(ymax)
        .append(' ')
        .append(zmax)
        .append(')');
  }
}
