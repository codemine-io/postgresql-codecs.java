package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class IntervalCodecTest extends CodecTestBase<Interval> {
  public IntervalCodecTest() {
    super(Codec.INTERVAL);
  }
}
