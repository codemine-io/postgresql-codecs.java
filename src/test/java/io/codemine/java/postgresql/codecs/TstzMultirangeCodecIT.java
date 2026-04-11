package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;
import java.time.Instant;

public class TstzMultirangeCodecIT extends CodecITBase<Multirange<Instant>> {
  @SuppressWarnings("unchecked")
  public TstzMultirangeCodecIT() {
    super(Codec.TSTZMULTIRANGE, (Class<Multirange<Instant>>) (Class<?>) Multirange.class);
  }
}
