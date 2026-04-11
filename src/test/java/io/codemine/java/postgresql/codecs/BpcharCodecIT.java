package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class BpcharCodecIT extends CodecITBase<String> {
  public BpcharCodecIT() {
    super(Codec.BPCHAR, String.class);
  }
}
