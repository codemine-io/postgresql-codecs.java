package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class OidCodecIT extends CodecITBase<Integer> {
  public OidCodecIT() {
    super(Codec.OID, Integer.class);
  }
}
