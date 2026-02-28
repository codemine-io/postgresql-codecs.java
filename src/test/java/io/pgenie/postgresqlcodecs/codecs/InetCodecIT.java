package io.pgenie.postgresqlcodecs.codecs;

import io.pgenie.postgresqlcodecs.types.Inet;

public class InetCodecIT extends CodecSuite<Inet> {
  public InetCodecIT() {
    super(Inet.CODEC, Inet.class);
  }
}
