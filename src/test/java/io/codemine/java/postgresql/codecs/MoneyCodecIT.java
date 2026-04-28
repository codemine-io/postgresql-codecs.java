package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;
import java.math.BigDecimal;

public class MoneyCodecIT extends CodecITBase<BigDecimal> {
  public MoneyCodecIT() {
    super(Codec.money(2), BigDecimal.class);
  }
}
