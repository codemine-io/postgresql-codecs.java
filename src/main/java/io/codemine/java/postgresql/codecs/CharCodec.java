package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL internal {@code "char"} (single-byte) type. */
final class CharCodec implements Codec<Byte> {

  @Override
  public String name() {
    return "char";
  }

  /**
   * PostgreSQL's internal {@code "char"} type requires double-quoting in SQL casts to distinguish
   * it from {@code char(N)} (bpchar, OID 1042). Without the quotes, {@code SELECT $1::char} targets
   * bpchar, not the internal "char" (OID 18).
   */
  @Override
  public String typeSig() {
    return "\"char\"";
  }

  @Override
  public int scalarOid() {
    return 18;
  }

  @Override
  public int arrayOid() {
    return 1002;
  }

  @Override
  public void encodeInText(StringBuilder sb, Byte value) {
    sb.append((char) (value & 0xFF));
  }

  @Override
  public Codec.ParsingResult<Byte> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    if (offset >= input.length()) {
      throw new Codec.DecodingException(input, offset, "Empty input for char");
    }
    byte value = (byte) input.charAt(offset);
    return new Codec.ParsingResult<>(value, offset + 1);
  }

  @Override
  public void encodeInBinary(Byte value, ByteArrayOutputStream out) {
    out.write(value & 0xFF);
  }

  @Override
  public Byte decodeInBinary(ByteBuffer buf, int length) {
    return buf.get();
  }

  @Override
  public Byte random(Random r, int size) {
    // Printable ASCII range: 32-126.
    return (byte) (r.nextInt(95) + 32);
  }
}
