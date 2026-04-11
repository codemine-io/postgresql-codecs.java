package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class TimetzCodecIT extends CodecITBase<Timetz> {
  public TimetzCodecIT() {
    super(Codec.TIMETZ, Timetz.class);
  }
}
