package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class LtreeCodecIT extends CodecITBase<Ltree> {
  public LtreeCodecIT() {
    super(Codec.LTREE, Ltree.class);
  }
}
