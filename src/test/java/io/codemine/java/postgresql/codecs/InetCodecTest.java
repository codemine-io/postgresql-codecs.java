package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class InetCodecTest extends CodecTestBase<Inet> {
  public InetCodecTest() {
    super(Codec.INET);
  }
}
