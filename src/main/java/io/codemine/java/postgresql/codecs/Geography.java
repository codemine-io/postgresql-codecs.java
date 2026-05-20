package io.codemine.java.postgresql.codecs;

import java.util.Objects;

/** PostGIS {@code geography} payload modeled as a structured geometry tree. */
public record Geography(Geometry value) {
  /** Creates a geography value backed by a non-null geometry tree. */
  public Geography {
    Objects.requireNonNull(value, "value");
  }
}
