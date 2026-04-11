package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class VarcharWithSizeCodecIT extends CodecITBase<String> {
  public VarcharWithSizeCodecIT() {
    super(Codec.varchar(10), String.class);
  }
}
