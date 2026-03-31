package io.codemine.postgresql.codecs;

public class TsvectorCodecIT extends CodecITBase<Tsvector> {
  public TsvectorCodecIT() {
    super(Codec.TSVECTOR, Tsvector.class);
  }
}
