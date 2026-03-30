package io.codemine.postgresql.codecs;

public class MacaddrCodecIT extends CodecITBase<MacaddrCodec.Macaddr> {
  public MacaddrCodecIT() {
    super(Codec.MACADDR, MacaddrCodec.Macaddr.class);
  }
}
