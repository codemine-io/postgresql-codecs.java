package io.codemine.postgresql.codecs;

/** PostgreSQL {@code point} type. A point on a plane: (x, y). */
public record Point(double x, double y) {
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('(');
    sb.append(Double.toString(x));
    sb.append(',');
    sb.append(Double.toString(y));
    sb.append(')');
    return sb.toString();
  }
}
