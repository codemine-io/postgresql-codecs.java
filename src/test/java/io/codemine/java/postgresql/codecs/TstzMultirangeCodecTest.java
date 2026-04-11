package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;
import java.time.Instant;

public class TstzMultirangeCodecTest extends CodecTestBase<Multirange<Instant>> {
  public TstzMultirangeCodecTest() {
    super(Codec.TSTZMULTIRANGE);
  }
}
