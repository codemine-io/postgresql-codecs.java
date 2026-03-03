package io.codemine.postgresql.codecs;

import io.codemine.postgresql.types.Inet;

public class InetCodecTest extends CodecTestBase<Inet> {
  public InetCodecTest() {
    super(Inet.CODEC, Inet.class);
  }
}
