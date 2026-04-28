package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class OidCodecTest extends CodecTestBase<Long> {
  public OidCodecTest() {
    super(Codec.OID);
  }
}
