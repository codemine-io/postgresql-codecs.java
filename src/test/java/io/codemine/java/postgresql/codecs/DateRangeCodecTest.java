package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;
import java.time.LocalDate;

public class DateRangeCodecTest extends CodecTestBase<Range<LocalDate>> {
  public DateRangeCodecTest() {
    super(Codec.DATERANGE);
  }
}
