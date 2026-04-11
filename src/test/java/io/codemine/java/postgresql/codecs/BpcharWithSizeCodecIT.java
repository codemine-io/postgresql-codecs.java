package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class BpcharWithSizeCodecIT extends CodecITBase<String> {
  public BpcharWithSizeCodecIT() {
    super(Codec.bpchar(5), String.class);
  }
}
