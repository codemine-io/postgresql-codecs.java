package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/** Codec for PostgreSQL {@code bool} values. */
final class BoolCodec implements Codec<Boolean> {

  @Override
  public String name() {
    return "bool";
  }

  @Override
  public int scalarOid() {
    return 16;
  }

  @Override
  public int arrayOid() {
    return 1000;
  }

  @Override
  public void encodeInText(StringBuilder sb, Boolean value) {
    sb.append(value ? 't' : 'f');
  }

  @Override
  public Codec.ParsingResult<Boolean> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    String s = input.subSequence(offset, input.length()).toString().trim();
    return switch (s) {
      case "t" -> new Codec.ParsingResult<>(true, input.length());
      case "f" -> new Codec.ParsingResult<>(false, input.length());
      default -> throw new Codec.DecodingException(input, offset, "Invalid bool: " + s);
    };
  }

  @Override
  public void encodeInBinary(Boolean value, ByteArrayOutputStream out) {
    out.write(value ? 1 : 0);
  }

  @Override
  public Boolean decodeInBinary(ByteBuffer buf, int length) {
    return buf.get() != 0;
  }

  @Override
  public Boolean random(Random r, int size) {
    return r.nextBoolean();
  }
}
