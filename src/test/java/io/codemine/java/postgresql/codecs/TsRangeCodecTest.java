package io.codemine.java.postgresql.codecs;

import java.time.LocalDateTime;

public class TsRangeCodecTest extends CodecTestBase<Range<LocalDateTime>> {
  public TsRangeCodecTest() {
    super(Codec.TSRANGE);
  }
}
