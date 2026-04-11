package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;
import java.time.LocalDateTime;

public class TimestampCodecIT extends CodecITBase<LocalDateTime> {
  public TimestampCodecIT() {
    super(Codec.TIMESTAMP, LocalDateTime.class);
  }
}
