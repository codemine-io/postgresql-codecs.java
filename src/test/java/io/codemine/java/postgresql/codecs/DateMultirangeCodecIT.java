package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;
import java.time.LocalDate;

public class DateMultirangeCodecIT extends CodecITBase<Multirange<LocalDate>> {
  @SuppressWarnings("unchecked")
  public DateMultirangeCodecIT() {
    super(Codec.DATEMULTIRANGE, (Class<Multirange<LocalDate>>) (Class<?>) Multirange.class);
  }
}
