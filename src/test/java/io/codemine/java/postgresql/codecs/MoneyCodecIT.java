package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class MoneyCodecIT extends CodecITBase<Money> {
  public MoneyCodecIT() {
    super(Codec.money(2), Money.class);
  }
}
