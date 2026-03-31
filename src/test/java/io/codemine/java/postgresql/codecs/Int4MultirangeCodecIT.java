package io.codemine.java.postgresql.codecs;

public class Int4MultirangeCodecIT extends CodecITBase<Multirange<Integer>> {
  @SuppressWarnings("unchecked")
  public Int4MultirangeCodecIT() {
    super(Codec.INT4MULTIRANGE, (Class<Multirange<Integer>>) (Class<?>) Multirange.class);
  }
}
