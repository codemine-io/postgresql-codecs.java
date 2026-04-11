package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class CircleCodecIT extends CodecITBase<Circle> {
  public CircleCodecIT() {
    super(Codec.CIRCLE, Circle.class);
  }
}
