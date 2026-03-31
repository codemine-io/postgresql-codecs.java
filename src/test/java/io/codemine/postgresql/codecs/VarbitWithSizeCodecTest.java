package io.codemine.postgresql.codecs;

public class VarbitWithSizeCodecTest extends CodecTestBase<Bit> {
  public VarbitWithSizeCodecTest() {
    super(Codec.varbit(16));
  }
}
