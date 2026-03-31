package io.codemine.java.postgresql.codecs;

import java.time.LocalDateTime;

public class TsMultirangeCodecIT extends CodecITBase<Multirange<LocalDateTime>> {
  @SuppressWarnings("unchecked")
  public TsMultirangeCodecIT() {
    super(Codec.TSMULTIRANGE, (Class<Multirange<LocalDateTime>>) (Class<?>) Multirange.class);
  }
}
