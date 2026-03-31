package io.codemine.java.postgresql.codecs;

import java.time.Instant;

public class TstzRangeCodecTest extends CodecTestBase<Range<Instant>> {
  public TstzRangeCodecTest() {
    super(Codec.TSTZRANGE);
  }
}
