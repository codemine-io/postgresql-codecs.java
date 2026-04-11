package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class BpcharWithSizeCodecTest extends CodecTestBase<String> {
  public BpcharWithSizeCodecTest() {
    super(Codec.bpchar(5));
  }
}
