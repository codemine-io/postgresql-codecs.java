package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class PolygonCodecTest extends CodecTestBase<Polygon> {
  public PolygonCodecTest() {
    super(Codec.POLYGON);
  }
}
