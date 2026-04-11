package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class CitextCodecTest extends CodecTestBase<String> {
  public CitextCodecTest() {
    super(Codec.CITEXT);
  }
}
