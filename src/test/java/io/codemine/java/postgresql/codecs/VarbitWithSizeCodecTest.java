package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class VarbitWithSizeCodecTest extends CodecTestBase<Bit> {
  public VarbitWithSizeCodecTest() {
    super(Codec.varbit(16));
  }
}
