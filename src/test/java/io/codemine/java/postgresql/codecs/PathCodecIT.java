package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class PathCodecIT extends CodecITBase<Path> {
  public PathCodecIT() {
    super(Codec.PATH, Path.class);
  }
}
