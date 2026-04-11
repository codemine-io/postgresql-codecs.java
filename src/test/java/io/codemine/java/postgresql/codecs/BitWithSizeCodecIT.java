package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class BitWithSizeCodecIT extends CodecITBase<Bit> {
  public BitWithSizeCodecIT() {
    super(Codec.bit(8), Bit.class);
  }
}
