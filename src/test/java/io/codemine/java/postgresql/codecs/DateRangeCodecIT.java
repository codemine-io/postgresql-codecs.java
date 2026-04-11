package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;
import java.time.LocalDate;

public class DateRangeCodecIT extends CodecITBase<Range<LocalDate>> {
  @SuppressWarnings("unchecked")
  public DateRangeCodecIT() {
    super(Codec.DATERANGE, (Class<Range<LocalDate>>) (Class<?>) Range.class);
  }
}
