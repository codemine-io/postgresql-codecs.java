package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/** Codec for PostgreSQL {@code jsonb} values. */
final class JsonbCodec implements Codec<String> {

  @Override
  public String name() {
    return "jsonb";
  }

  @Override
  public int scalarOid() {
    return 3802;
  }

  @Override
  public int arrayOid() {
    return 3807;
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
    out.write(1); // JSONB version byte
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    out.write(bytes, 0, bytes.length);
  }

  @Override
  public String decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    byte version = buf.get();
    if (version != 1) {
      throw new Codec.DecodingException("Unsupported jsonb version: " + version);
    }
    byte[] bytes = new byte[length - 1];
    buf.get(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  @Override
  public String random(Random r, int size) {
    return JsonCodec.randomJson(r, size);
  }
}
