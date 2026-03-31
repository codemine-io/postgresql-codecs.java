package io.codemine.java.postgresql.codecs;

public class OidCodecIT extends CodecITBase<Integer> {
  public OidCodecIT() {
    super(Codec.OID, Integer.class);
  }
}
