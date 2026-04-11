package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class CharCodecTest extends CodecTestBase<Byte> {
  public CharCodecTest() {
    super(Codec.CHAR);
  }
}
