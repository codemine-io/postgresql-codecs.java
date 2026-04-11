package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class BoxCodecIT extends CodecITBase<Box> {
  public BoxCodecIT() {
    super(Codec.BOX, Box.class);
  }
}
