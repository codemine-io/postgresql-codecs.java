package io.codemine.java.postgresql.codecs;

import java.time.LocalDate;

public class DateCodecTest extends CodecTestBase<LocalDate> {
  public DateCodecTest() {
    super(Codec.DATE);
  }
}
