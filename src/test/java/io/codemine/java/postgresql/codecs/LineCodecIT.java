package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class LineCodecIT extends CodecITBase<Line> {
  public LineCodecIT() {
    super(Codec.LINE, Line.class);
  }
}
