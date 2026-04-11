package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;
import java.math.BigDecimal;

public class NumRangeCodecTest extends CodecTestBase<Range<BigDecimal>> {
  public NumRangeCodecTest() {
    super(Codec.NUMRANGE);
  }
}
