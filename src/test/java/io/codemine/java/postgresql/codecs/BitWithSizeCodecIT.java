package io.codemine.java.postgresql.codecs;

public class BitWithSizeCodecIT extends CodecITBase<Bit> {
  public BitWithSizeCodecIT() {
    super(Codec.bit(8), Bit.class);
  }
}
