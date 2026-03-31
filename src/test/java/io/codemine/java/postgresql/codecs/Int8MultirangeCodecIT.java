package io.codemine.java.postgresql.codecs;

public class Int8MultirangeCodecIT extends CodecITBase<Multirange<Long>> {
  @SuppressWarnings("unchecked")
  public Int8MultirangeCodecIT() {
    super(Codec.INT8MULTIRANGE, (Class<Multirange<Long>>) (Class<?>) Multirange.class);
  }
}
