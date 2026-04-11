package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;

public class Int4RangeCodecIT extends CodecITBase<Range<Integer>> {
  @SuppressWarnings("unchecked")
  public Int4RangeCodecIT() {
    super(Codec.INT4RANGE, (Class<Range<Integer>>) (Class<?>) Range.class);
  }
}
