package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class VarcharCodecIT extends CodecITBase<String> {
  public VarcharCodecIT() {
    super(Codec.VARCHAR, String.class);
  }
}
