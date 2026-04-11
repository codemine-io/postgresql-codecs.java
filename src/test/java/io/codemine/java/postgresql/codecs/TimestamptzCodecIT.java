package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;
import java.time.Instant;

public class TimestamptzCodecIT extends CodecITBase<Instant> {
  public TimestamptzCodecIT() {
    super(Codec.TIMESTAMPTZ, Instant.class);
  }
}
