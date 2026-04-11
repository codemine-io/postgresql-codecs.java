package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class BoolCodecIT extends CodecITBase<Boolean> {
  public BoolCodecIT() {
    super(Codec.BOOL, Boolean.class);
  }
}
