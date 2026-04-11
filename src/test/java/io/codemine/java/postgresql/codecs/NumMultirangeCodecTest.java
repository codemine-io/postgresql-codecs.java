package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;
import java.math.BigDecimal;

public class NumMultirangeCodecTest extends CodecTestBase<Multirange<BigDecimal>> {
  public NumMultirangeCodecTest() {
    super(Codec.NUMMULTIRANGE);
  }
}
