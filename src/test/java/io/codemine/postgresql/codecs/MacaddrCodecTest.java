package io.codemine.postgresql.codecs;

import io.codemine.postgresql.types.Macaddr;

public class MacaddrCodecTest extends CodecTestSuite<Macaddr> {
  public MacaddrCodecTest() {
    super(Macaddr.CODEC, Macaddr.class);
  }
}
