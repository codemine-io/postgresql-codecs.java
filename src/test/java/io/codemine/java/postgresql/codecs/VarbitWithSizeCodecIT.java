package io.codemine.java.postgresql.codecs;

public class VarbitWithSizeCodecIT extends CodecITBase<Bit> {
  public VarbitWithSizeCodecIT() {
    super(Codec.varbit(16), Bit.class);
  }
}
