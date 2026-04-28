package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class OidCodecIT extends CodecITBase<Long> {
  public OidCodecIT() {
    super(Codec.OID, Long.class);
  }
}
