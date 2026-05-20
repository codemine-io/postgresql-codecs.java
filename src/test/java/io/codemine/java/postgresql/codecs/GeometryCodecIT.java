package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

class GeometryCodecIT extends CodecITBase<Geometry> {
  GeometryCodecIT() {
    super(Codec.GEOMETRY, Geometry.class);
  }
}
