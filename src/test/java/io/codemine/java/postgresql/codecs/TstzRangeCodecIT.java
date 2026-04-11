package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;
import java.time.Instant;

public class TstzRangeCodecIT extends CodecITBase<Range<Instant>> {
  @SuppressWarnings("unchecked")
  public TstzRangeCodecIT() {
    super(Codec.TSTZRANGE, (Class<Range<Instant>>) (Class<?>) Range.class);
  }
}
