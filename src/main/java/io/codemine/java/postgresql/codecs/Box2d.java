package io.codemine.java.postgresql.codecs;

/** PostGIS {@code box2d} type. */
public record Box2d(double xmin, double ymin, double xmax, double ymax) {

  /** Builds a canonical box from any two opposite corners. */
  public static Box2d of(double x1, double y1, double x2, double y2) {
    return new Box2d(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2));
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendInTextTo(sb);
    return sb.toString();
  }

  void appendInTextTo(StringBuilder sb) {
    sb.append("BOX(")
        .append(xmin)
        .append(' ')
        .append(ymin)
        .append(',')
        .append(xmax)
        .append(' ')
        .append(ymax)
        .append(')');
  }
}
