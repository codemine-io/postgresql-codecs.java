package io.codemine.postgresql.codecs;

import io.codemine.postgresql.types.Macaddr;

public class MacaddrCodecTest extends CodecTestBase<Macaddr> {
  public MacaddrCodecTest() {
    super(Macaddr.CODEC, Macaddr.class);
  }
}
