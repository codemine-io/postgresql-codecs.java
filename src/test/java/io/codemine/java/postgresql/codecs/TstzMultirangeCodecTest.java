package io.codemine.java.postgresql.codecs;

import java.time.Instant;

public class TstzMultirangeCodecTest extends CodecTestBase<Multirange<Instant>> {
  public TstzMultirangeCodecTest() {
    super(Codec.TSTZMULTIRANGE);
  }
}
