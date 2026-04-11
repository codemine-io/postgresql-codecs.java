package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class Int4CodecIT extends CodecITBase<Integer> {
  public Int4CodecIT() {
    super(Codec.INT4, Integer.class);
  }
}
