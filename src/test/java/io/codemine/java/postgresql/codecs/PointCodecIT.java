package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class PointCodecIT extends CodecITBase<Point> {
  public PointCodecIT() {
    super(Codec.POINT, Point.class);
  }
}
