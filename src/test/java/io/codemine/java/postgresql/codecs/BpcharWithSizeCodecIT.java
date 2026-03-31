package io.codemine.java.postgresql.codecs;

public class BpcharWithSizeCodecIT extends CodecITBase<String> {
  public BpcharWithSizeCodecIT() {
    super(Codec.bpchar(5), String.class);
  }
}
