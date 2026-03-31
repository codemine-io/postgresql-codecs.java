package io.codemine.java.postgresql.codecs;

import java.util.UUID;

public class UuidCodecTest extends CodecTestBase<UUID> {
  public UuidCodecTest() {
    super(Codec.UUID);
  }
}
