package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class ByteaCodecTest extends CodecTestBase<Bytea> {
  public ByteaCodecTest() {
    super(Codec.BYTEA);
  }
}
