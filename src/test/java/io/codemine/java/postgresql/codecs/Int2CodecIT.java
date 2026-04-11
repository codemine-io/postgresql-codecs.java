package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class Int2CodecIT extends CodecITBase<Short> {
  public Int2CodecIT() {
    super(Codec.INT2, Short.class);
  }
}
