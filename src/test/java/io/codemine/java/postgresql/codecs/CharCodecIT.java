package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class CharCodecIT extends CodecITBase<Byte> {
  public CharCodecIT() {
    super(Codec.CHAR, Byte.class);
  }
}
