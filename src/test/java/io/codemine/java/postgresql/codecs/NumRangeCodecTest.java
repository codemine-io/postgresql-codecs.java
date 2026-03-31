package io.codemine.java.postgresql.codecs;

import java.math.BigDecimal;

public class NumRangeCodecTest extends CodecTestBase<Range<BigDecimal>> {
  public NumRangeCodecTest() {
    super(Codec.NUMRANGE);
  }
}
