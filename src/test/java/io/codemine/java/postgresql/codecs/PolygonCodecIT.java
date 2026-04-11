package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class PolygonCodecIT extends CodecITBase<Polygon> {
  public PolygonCodecIT() {
    super(Codec.POLYGON, Polygon.class);
  }
}
