package io.codemine.java.postgresql.codecs;

public class VarbitCodecIT extends CodecITBase<Bit> {
  public VarbitCodecIT() {
    super(Codec.VARBIT, Bit.class);
  }
}
