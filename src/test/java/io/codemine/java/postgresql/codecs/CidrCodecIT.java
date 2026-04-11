package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class CidrCodecIT extends CodecITBase<Cidr> {
  public CidrCodecIT() {
    super(Codec.CIDR, Cidr.class);
  }
}
