package io.codemine.java.postgresql.codecs;

import java.time.LocalTime;

public class TimeCodecTest extends CodecTestBase<LocalTime> {
  public TimeCodecTest() {
    super(Codec.TIME);
  }
}
