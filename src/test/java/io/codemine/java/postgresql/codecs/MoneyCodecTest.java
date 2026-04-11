package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class MoneyCodecTest extends CodecTestBase<Long> {
  public MoneyCodecTest() {
    super(Codec.MONEY);
  }
}
