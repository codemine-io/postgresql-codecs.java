package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code bit} (fixed-length bit string) values. */
final class BitCodec implements Codec<Bit> {

  @Override
  public String name() {
    return "bit";
  }

  @Override
  public int scalarOid() {
    return 1560;
  }

  @Override
  public int arrayOid() {
    return 1561;
  }

  @Override
  public void write(StringBuilder sb, Bit value) {
    BitVarbitUtil.write(sb, value);
  }

  @Override
  public Codec.ParsingResult<Bit> parse(CharSequence input, int offset)
      throws Codec.DecodingException {
    return BitVarbitUtil.parse(input, offset);
  }

  @Override
  public void encodeInBinary(Bit value, ByteArrayOutputStream out) {
    BitVarbitUtil.encodeInBinary(value, out);
  }

  @Override
  public Bit decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    return BitVarbitUtil.decodeInBinary(buf, length);
  }

  @Override
  public Bit random(Random r, int size) {
    return BitVarbitUtil.random(r, size);
  }
}
