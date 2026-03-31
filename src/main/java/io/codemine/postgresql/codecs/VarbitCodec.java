package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code varbit} (variable-length bit string) values. */
final class VarbitCodec implements Codec<Bit> {

  private final int maxSize;

  VarbitCodec() {
    this(0);
  }

  VarbitCodec(int maxSize) {
    if (maxSize < 0) throw new IllegalArgumentException("maxSize must be >= 0, got: " + maxSize);
    this.maxSize = maxSize;
  }

  @Override
  public String name() {
    return "varbit";
  }

  @Override
  public String typeSig() {
    return maxSize > 0 ? "varbit(" + maxSize + ")" : "varbit";
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
  public void encodeInText(StringBuilder sb, Bit value) {
    Codec.BIT.encodeInText(sb, value);
  }

  @Override
  public Codec.ParsingResult<Bit> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    return Codec.BIT.decodeInText(input, offset);
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
    int effectiveMax = maxSize > 0 ? maxSize : size;
    return Codec.BIT.random(r, effectiveMax);
  }
}
