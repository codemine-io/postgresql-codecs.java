package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class BitCodecIT extends CodecITBase<Bit> {
  public BitCodecIT() {
    super(Codec.BIT, Bit.class);
  }
}
