package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class HstoreCodecTest extends CodecTestBase<Hstore> {
  public HstoreCodecTest() {
    super(Codec.HSTORE);
  }
}
