package io.codemine.postgresql.codecs;

import java.time.Instant;

public class TstzMultirangeCodecIT extends CodecITBase<Multirange<Instant>> {
  @SuppressWarnings("unchecked")
  public TstzMultirangeCodecIT() {
    super(Codec.TSTZMULTIRANGE, (Class<Multirange<Instant>>) (Class<?>) Multirange.class);
  }
}
