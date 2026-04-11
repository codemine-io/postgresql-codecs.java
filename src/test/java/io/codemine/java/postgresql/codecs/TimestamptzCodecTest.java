package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;
import java.time.Instant;

public class TimestamptzCodecTest extends CodecTestBase<Instant> {
  public TimestamptzCodecTest() {
    super(Codec.TIMESTAMPTZ);
  }
}
