package io.codemine.java.postgresql.codecs;

public class VarcharCodecIT extends CodecITBase<String> {
  public VarcharCodecIT() {
    super(Codec.VARCHAR, String.class);
  }
}
