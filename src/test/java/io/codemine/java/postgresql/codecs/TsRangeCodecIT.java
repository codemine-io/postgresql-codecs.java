package io.codemine.java.postgresql.codecs;

import java.time.LocalDateTime;

public class TsRangeCodecIT extends CodecITBase<Range<LocalDateTime>> {
  @SuppressWarnings("unchecked")
  public TsRangeCodecIT() {
    super(Codec.TSRANGE, (Class<Range<LocalDateTime>>) (Class<?>) Range.class);
  }
}
