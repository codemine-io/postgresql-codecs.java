package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;
import java.math.BigDecimal;

public class NumRangeCodecIT extends CodecITBase<Range<BigDecimal>> {
  @SuppressWarnings("unchecked")
  public NumRangeCodecIT() {
    super(Codec.NUMRANGE, (Class<Range<BigDecimal>>) (Class<?>) Range.class);
  }
}
