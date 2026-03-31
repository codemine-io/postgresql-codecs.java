package io.codemine.java.postgresql.codecs;

import java.math.BigDecimal;

public class NumMultirangeCodecTest extends CodecTestBase<Multirange<BigDecimal>> {
  public NumMultirangeCodecTest() {
    super(Codec.NUMMULTIRANGE);
  }
}
