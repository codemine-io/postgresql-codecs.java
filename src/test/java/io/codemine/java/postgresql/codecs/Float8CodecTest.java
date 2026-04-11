package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class Float8CodecTest extends CodecTestBase<Double> {
  public Float8CodecTest() {
    super(Codec.FLOAT8);
  }
}
