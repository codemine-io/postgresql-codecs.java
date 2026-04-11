package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;
import java.math.BigDecimal;

public class NumericCodecIT extends CodecITBase<BigDecimal> {
  public NumericCodecIT() {
    super(Codec.NUMERIC, BigDecimal.class);
  }
}
