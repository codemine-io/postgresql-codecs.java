package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

class GeographyCodecIT extends CodecITBase<Geography> {
  GeographyCodecIT() {
    super(Codec.GEOGRAPHY, Geography.class);
  }
}
