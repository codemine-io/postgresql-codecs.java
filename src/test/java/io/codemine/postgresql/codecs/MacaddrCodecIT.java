package io.codemine.postgresql.codecs;

import io.codemine.postgresql.types.Macaddr;

public class MacaddrCodecIT extends CodecSuite<Macaddr> {
  public MacaddrCodecIT() {
    super(Macaddr.CODEC, Macaddr.class);
  }
}
