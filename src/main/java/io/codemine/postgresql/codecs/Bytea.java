package io.codemine.postgresql.codecs;

import java.util.Arrays;
import java.util.HexFormat;

/** A wrapper around a byte array representing a PostgreSQL {@code bytea} value. */
public record Bytea(byte[] bytes) {

  @Override
  public boolean equals(Object o) {
    return o instanceof Bytea b && Arrays.equals(bytes, b.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

  @Override
  public String toString() {
    return "\\x" + HexFormat.of().formatHex(bytes);
  }
}
