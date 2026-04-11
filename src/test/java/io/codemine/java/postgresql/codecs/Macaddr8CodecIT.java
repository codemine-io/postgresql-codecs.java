package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class Macaddr8CodecIT extends CodecITBase<Macaddr8> {
  public Macaddr8CodecIT() {
    super(Codec.MACADDR8, Macaddr8.class);
  }
}
