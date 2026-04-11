package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class OidCodecTest extends CodecTestBase<Integer> {
  public OidCodecTest() {
    super(Codec.OID);
  }
}
