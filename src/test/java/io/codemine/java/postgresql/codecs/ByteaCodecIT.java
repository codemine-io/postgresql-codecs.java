package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class ByteaCodecIT extends CodecITBase<Bytea> {
  public ByteaCodecIT() {
    super(Codec.BYTEA, Bytea.class);
  }
}
