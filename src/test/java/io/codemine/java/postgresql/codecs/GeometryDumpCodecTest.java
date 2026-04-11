package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecTestBase;

class GeometryDumpCodecTest extends CodecTestBase<GeometryDump> {
  GeometryDumpCodecTest() {
    super(Codec.GEOMETRY_DUMP);
  }
}
