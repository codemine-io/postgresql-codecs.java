package io.codemine.postgresql.codecs;

public class BpcharWithSizeCodecTest extends CodecTestBase<String> {
  public BpcharWithSizeCodecTest() {
    super(Codec.bpchar(5));
  }
}
