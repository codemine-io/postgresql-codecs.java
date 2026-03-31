package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code varbit} (variable-length bit string) values. */
final class VarbitCodec implements Codec<Bit> {

  @Override
  public String name() {
    return "varbit";
  }

  @Override
  public int scalarOid() {
    return 1562;
  }

  @Override
  public int arrayOid() {
    return 1563;
  }

  @Override
  public void render(StringBuilder sb, Bit value) {
    Codec.BIT.render(sb, value);
  }

  @Override
  public Codec.ParsingResult<Bit> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    return Codec.BIT.parse(input, offset);
  }

  @Override
  public void encodeInBinary(Bit value, ByteArrayOutputStream out) {
    Codec.BIT.encodeInBinary(value, out);
  }

  @Override
  public Bit decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    return Codec.BIT.decodeInBinary(buf, length);
  }

  @Override
  public Bit random(Random r, int size) {
    return Codec.BIT.random(r, size);
  }
}
