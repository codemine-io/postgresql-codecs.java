package io.codemine.postgresql.codecs;

public class Int4RangeCodecIT extends CodecITBase<Range<Integer>> {
  @SuppressWarnings("unchecked")
  public Int4RangeCodecIT() {
    super(Codec.INT4RANGE, (Class<Range<Integer>>) (Class<?>) Range.class);
  }
}
