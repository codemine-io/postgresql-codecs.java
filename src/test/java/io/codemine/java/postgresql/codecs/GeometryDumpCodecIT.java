package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

class GeometryDumpCodecIT extends CodecITBase<GeometryDump> {
  GeometryDumpCodecIT() {
    super(Codec.GEOMETRY_DUMP, GeometryDump.class);
  }
}
