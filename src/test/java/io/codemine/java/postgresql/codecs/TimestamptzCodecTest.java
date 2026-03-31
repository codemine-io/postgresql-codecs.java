package io.codemine.java.postgresql.codecs;

import java.time.Instant;

public class TimestamptzCodecTest extends CodecTestBase<Instant> {
  public TimestamptzCodecTest() {
    super(Codec.TIMESTAMPTZ);
  }
}
