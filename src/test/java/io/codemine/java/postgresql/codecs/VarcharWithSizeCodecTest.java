package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class VarcharWithSizeCodecTest extends CodecTestBase<String> {
  public VarcharWithSizeCodecTest() {
    super(Codec.varchar(10));
  }
}
