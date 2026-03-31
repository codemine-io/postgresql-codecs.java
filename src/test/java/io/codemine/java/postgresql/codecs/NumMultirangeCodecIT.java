package io.codemine.java.postgresql.codecs;

import java.math.BigDecimal;

public class NumMultirangeCodecIT extends CodecITBase<Multirange<BigDecimal>> {
  @SuppressWarnings("unchecked")
  public NumMultirangeCodecIT() {
    super(Codec.NUMMULTIRANGE, (Class<Multirange<BigDecimal>>) (Class<?>) Multirange.class);
  }
}
