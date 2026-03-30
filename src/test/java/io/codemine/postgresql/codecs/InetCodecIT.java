package io.codemine.postgresql.codecs;

public class InetCodecIT extends CodecITBase<InetCodec.Inet> {
  public InetCodecIT() {
    super(Codec.INET, InetCodec.Inet.class);
  }
}
