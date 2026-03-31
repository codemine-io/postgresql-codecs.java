package io.codemine.postgresql.codecs;

public class BitWithSizeCodecTest extends CodecTestBase<Bit> {
  public BitWithSizeCodecTest() {
    super(Codec.bit(8));
  }
}
