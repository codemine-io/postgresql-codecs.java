package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;
import java.time.LocalDateTime;

public class TimestampCodecTest extends CodecTestBase<LocalDateTime> {
  public TimestampCodecTest() {
    super(Codec.TIMESTAMP);
  }
}
