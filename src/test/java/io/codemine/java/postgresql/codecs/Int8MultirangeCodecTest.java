package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class Int8MultirangeCodecTest extends CodecTestBase<Multirange<Long>> {
  public Int8MultirangeCodecTest() {
    super(Codec.INT8MULTIRANGE);
  }
}
