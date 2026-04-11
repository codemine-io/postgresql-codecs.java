package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class BitWithSizeCodecTest extends CodecTestBase<Bit> {
  public BitWithSizeCodecTest() {
    super(Codec.bit(8));
  }
}
