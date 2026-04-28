package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class MoneyCodecTest extends CodecTestBase<Money> {
  public MoneyCodecTest() {
    super(Codec.money(2));
  }
}
