package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class VarcharCodecTest extends CodecTestBase<String> {
  public VarcharCodecTest() {
    super(Codec.VARCHAR);
  }
}
