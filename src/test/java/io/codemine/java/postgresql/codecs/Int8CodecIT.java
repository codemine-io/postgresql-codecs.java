package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class Int8CodecIT extends CodecITBase<Long> {
  public Int8CodecIT() {
    super(Codec.INT8, Long.class);
  }
}
