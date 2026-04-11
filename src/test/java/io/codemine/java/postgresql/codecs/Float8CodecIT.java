package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class Float8CodecIT extends CodecITBase<Double> {
  public Float8CodecIT() {
    super(Codec.FLOAT8, Double.class);
  }
}
