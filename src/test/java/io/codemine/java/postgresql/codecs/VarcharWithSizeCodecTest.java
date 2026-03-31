package io.codemine.java.postgresql.codecs;

public class VarcharWithSizeCodecTest extends CodecTestBase<String> {
  public VarcharWithSizeCodecTest() {
    super(Codec.varchar(10));
  }
}
