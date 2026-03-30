package io.codemine.postgresql.codecs;

public class MacaddrCodecTest extends CodecTestBase<MacaddrCodec.Macaddr> {
  public MacaddrCodecTest() {
    super(Codec.MACADDR, MacaddrCodec.Macaddr.class);
  }
}
