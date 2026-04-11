package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class TsvectorCodecTest extends CodecTestBase<Tsvector> {
  public TsvectorCodecTest() {
    super(Codec.TSVECTOR);
  }
}
