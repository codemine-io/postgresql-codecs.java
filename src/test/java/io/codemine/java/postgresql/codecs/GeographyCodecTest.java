package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

class GeographyCodecTest extends CodecTestBase<Geography> {
  GeographyCodecTest() {
    super(Codec.GEOGRAPHY);
  }
}
