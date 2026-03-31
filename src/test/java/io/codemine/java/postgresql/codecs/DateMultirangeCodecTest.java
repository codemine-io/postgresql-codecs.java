package io.codemine.java.postgresql.codecs;

import java.time.LocalDate;

public class DateMultirangeCodecTest extends CodecTestBase<Multirange<LocalDate>> {
  public DateMultirangeCodecTest() {
    super(Codec.DATEMULTIRANGE);
  }
}
