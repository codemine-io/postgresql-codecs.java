package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class Int8RangeCodecTest extends CodecTestBase<Range<Long>> {
  public Int8RangeCodecTest() {
    super(Codec.INT8RANGE);
  }
}
