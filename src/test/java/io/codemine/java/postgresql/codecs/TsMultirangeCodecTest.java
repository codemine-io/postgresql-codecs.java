package io.codemine.java.postgresql.codecs;

import java.time.LocalDateTime;

public class TsMultirangeCodecTest extends CodecTestBase<Multirange<LocalDateTime>> {
  public TsMultirangeCodecTest() {
    super(Codec.TSMULTIRANGE);
  }
}
