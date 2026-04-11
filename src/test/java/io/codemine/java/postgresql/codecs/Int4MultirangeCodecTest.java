package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class Int4MultirangeCodecTest extends CodecTestBase<Multirange<Integer>> {
  public Int4MultirangeCodecTest() {
    super(Codec.INT4MULTIRANGE);
  }
}
