package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class VarbitCodecIT extends CodecITBase<Bit> {
  public VarbitCodecIT() {
    super(Codec.VARBIT, Bit.class);
  }
}
