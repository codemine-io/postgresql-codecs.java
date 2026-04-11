package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class Float4CodecIT extends CodecITBase<Float> {
  public Float4CodecIT() {
    super(Codec.FLOAT4, Float.class);
  }
}
