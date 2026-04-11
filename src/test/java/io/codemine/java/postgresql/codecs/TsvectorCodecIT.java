package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class TsvectorCodecIT extends CodecITBase<Tsvector> {
  public TsvectorCodecIT() {
    super(Codec.TSVECTOR, Tsvector.class);
  }
}
