package io.codemine.java.postgresql.codecs;

import io.codemine.java.postgresql.CodecITBase;
import java.util.UUID;

public class UuidCodecIT extends CodecITBase<UUID> {
  public UuidCodecIT() {
    super(Codec.UUID, UUID.class);
  }
}
