package io.codemine.java.postgresql.codecs;

public class IntervalCodecIT extends CodecITBase<Interval> {
  public IntervalCodecIT() {
    super(Codec.INTERVAL, Interval.class);
  }
}
