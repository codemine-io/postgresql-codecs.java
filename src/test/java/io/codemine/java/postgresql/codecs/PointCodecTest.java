package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

public class PointCodecTest extends CodecTestBase<Point> {
  public PointCodecTest() {
    super(Codec.POINT);
  }
}
