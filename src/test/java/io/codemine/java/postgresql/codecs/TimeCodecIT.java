package io.codemine.java.postgresql.codecs;

import java.time.LocalTime;

public class TimeCodecIT extends CodecITBase<LocalTime> {
  public TimeCodecIT() {
    super(Codec.TIME, LocalTime.class);
  }
}
