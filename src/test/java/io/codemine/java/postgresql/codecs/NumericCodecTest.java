package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;
import java.math.BigDecimal;

public class NumericCodecTest extends CodecTestBase<BigDecimal> {
  public NumericCodecTest() {
    super(Codec.NUMERIC);
  }
}
