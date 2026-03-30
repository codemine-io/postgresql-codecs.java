package io.codemine.postgresql.codecs;

public class TextCodecIT extends CodecITBase<String> {
  public TextCodecIT() {
    super(Codec.TEXT, String.class);
  }
}
