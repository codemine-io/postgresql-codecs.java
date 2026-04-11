package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class InetCodecIT extends CodecITBase<Inet> {
  public InetCodecIT() {
    super(Codec.INET, Inet.class);
  }
}
