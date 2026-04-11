package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class BpcharCodecTest extends CodecTestBase<String> {
  public BpcharCodecTest() {
    super(Codec.BPCHAR);
  }
}
