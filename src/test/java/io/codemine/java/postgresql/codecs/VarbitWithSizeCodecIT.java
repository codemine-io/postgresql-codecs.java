package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class VarbitWithSizeCodecIT extends CodecITBase<Bit> {
  public VarbitWithSizeCodecIT() {
    super(Codec.varbit(16), Bit.class);
  }
}
