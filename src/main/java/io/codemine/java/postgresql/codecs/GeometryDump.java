package io.codemine.java.postgresql.codecs;

import java.util.List;
import java.util.Objects;

/** PostGIS {@code geometry_dump} composite. */
public record GeometryDump(Geometry geom, List<Integer> path) {
  /** Creates a geometry dump with a non-null geometry and path. */
  public GeometryDump {
    Objects.requireNonNull(geom, "geom");
    Objects.requireNonNull(path, "path");
    path = List.copyOf(path);
  }
}
