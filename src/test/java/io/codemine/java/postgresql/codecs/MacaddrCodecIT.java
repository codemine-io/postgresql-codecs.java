package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class MacaddrCodecIT extends CodecITBase<Macaddr> {
  public MacaddrCodecIT() {
    super(Codec.MACADDR, Macaddr.class);
  }
}
