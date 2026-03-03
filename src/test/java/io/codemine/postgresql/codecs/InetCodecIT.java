package io.codemine.postgresql.codecs;

import io.codemine.postgresql.types.Inet;

public class InetCodecIT extends CodecITBase<Inet> {
  public InetCodecIT() {
    super(Inet.CODEC, Inet.class);
  }
}
