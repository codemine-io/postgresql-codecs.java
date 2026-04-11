package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;
import java.time.Instant;

public class TstzRangeCodecTest extends CodecTestBase<Range<Instant>> {
  public TstzRangeCodecTest() {
    super(Codec.TSTZRANGE);
  }
}
