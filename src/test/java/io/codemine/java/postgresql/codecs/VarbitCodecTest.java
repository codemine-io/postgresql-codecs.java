package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class VarbitCodecTest extends CodecTestBase<Bit> {
  public VarbitCodecTest() {
    super(Codec.VARBIT);
  }
}
