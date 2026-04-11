package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class MoneyCodecIT extends CodecITBase<Long> {
  public MoneyCodecIT() {
    super(Codec.MONEY, Long.class);
  }
}
