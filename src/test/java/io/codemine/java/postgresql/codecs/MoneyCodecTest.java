package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;
import java.math.BigDecimal;

public class MoneyCodecTest extends CodecTestBase<BigDecimal> {
  public MoneyCodecTest() {
    super(Codec.money(2));
  }
}
