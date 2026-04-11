package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class TextCodecIT extends CodecITBase<String> {
  public TextCodecIT() {
    super(Codec.TEXT, String.class);
  }
}
