package io.codemine.java.postgresql.codecs;

public class Int8RangeCodecIT extends CodecITBase<Range<Long>> {
  @SuppressWarnings("unchecked")
  public Int8RangeCodecIT() {
    super(Codec.INT8RANGE, (Class<Range<Long>>) (Class<?>) Range.class);
  }
}
