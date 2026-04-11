package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;
import java.time.LocalDate;

public class DateCodecIT extends CodecITBase<LocalDate> {
  public DateCodecIT() {
    super(Codec.DATE, LocalDate.class);
  }
}
