package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class LsegCodecIT extends CodecITBase<Lseg> {
  public LsegCodecIT() {
    super(Codec.LSEG, Lseg.class);
  }
}
