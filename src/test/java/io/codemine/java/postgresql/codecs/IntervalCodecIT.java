package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class IntervalCodecIT extends CodecITBase<Interval> {
  public IntervalCodecIT() {
    super(Codec.INTERVAL, Interval.class);
  }
}
