package io.codemine.java.postgresql.codecs;

public class VarcharWithSizeCodecIT extends CodecITBase<String> {
  public VarcharWithSizeCodecIT() {
    super(Codec.varchar(10), String.class);
  }
}
