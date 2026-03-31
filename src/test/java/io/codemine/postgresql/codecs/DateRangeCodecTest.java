package io.codemine.postgresql.codecs;

import java.time.LocalDate;

public class DateRangeCodecTest extends CodecTestBase<Range<LocalDate>> {
  public DateRangeCodecTest() {
    super(Codec.DATERANGE);
  }
}
