package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/** Codec for PostgreSQL {@code varchar} values. */
final class VarcharCodec implements Codec<String> {

  @Override
  public String name() {
    return "varchar";
  }

  @Override
  public int scalarOid() {
    return 1043;
  }

  @Override
  public int arrayOid() {
    return 1015;
  }

  @Override
  public void write(StringBuilder sb, String value) {
    sb.append(value);
  }

  @Override
  public Codec.ParsingResult<String> parse(CharSequence input, int offset) {
    return new Codec.ParsingResult<>(
        input.subSequence(offset, input.length()).toString(), input.length());
  }

  @Override
  public void encodeInBinary(String value, ByteArrayOutputStream out) {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    out.write(bytes, 0, bytes.length);
  }

  @Override
  public String decodeInBinary(ByteBuffer buf, int length) {
    byte[] bytes = new byte[length];
    buf.get(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  @Override
  public String random(Random r, int size) {
    // Reuse TextCodec's random string generation logic.
    final int range1Size = 0xD7FF;
    final int totalValid = range1Size + (0x10FFFF - 0xE000 + 1);
    StringBuilder sb = new StringBuilder(size);
    for (int i = 0; i < size; i++) {
      int n = r.nextInt(totalValid);
      int codePoint = (n < range1Size) ? n + 1 : n + (0xE000 - range1Size);
      sb.appendCodePoint(codePoint);
    }
    return sb.toString();
  }
}
