package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Codec for PostgreSQL {@code ltree} values. */
final class LtreeCodec implements Codec<Ltree> {

  @Override
  public String name() {
    return "ltree";
  }

  @Override
  public int scalarOid() {
    return 0;
  }

  @Override
  public int arrayOid() {
    return 0;
  }

  @Override
  public void encodeInText(StringBuilder sb, Ltree value) {
    value.appendInTextTo(sb);
  }

  @Override
  public Codec.ParsingResult<Ltree> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    String text = input.subSequence(offset, input.length()).toString();
    if (text.isEmpty()) {
      return new Codec.ParsingResult<>(new Ltree(List.of()), input.length());
    }

    String[] rawLabels = text.split("\\.", -1);
    List<String> labels = new ArrayList<>(rawLabels.length);
    for (String label : rawLabels) {
      validateLabel(input, offset, label);
      labels.add(label);
    }
    return new Codec.ParsingResult<>(new Ltree(labels), input.length());
  }

  @Override
  public void encodeInBinary(Ltree value, ByteArrayOutputStream out) {
    out.write(1);
    byte[] bytes = value.toString().getBytes(StandardCharsets.UTF_8);
    out.write(bytes, 0, bytes.length);
  }

  @Override
  public Ltree decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    if (length < 1) {
      throw new Codec.DecodingException("Missing ltree binary version byte");
    }

    int version = Byte.toUnsignedInt(buf.get());
    if (version != 1) {
      throw new Codec.DecodingException("Unsupported ltree version number " + version);
    }

    byte[] bytes = new byte[length - 1];
    buf.get(bytes);
    return decodeInText(new String(bytes, StandardCharsets.UTF_8), 0).value;
  }

  @Override
  public Ltree random(Random r, int size) {
    int numLabels = size == 0 ? 0 : r.nextInt(Math.min(size, 10) + 1);
    List<String> labels = new ArrayList<>(numLabels);
    for (int i = 0; i < numLabels; i++) {
      labels.add(randomLabel(r, Math.max(1, Math.min(size, 12))));
    }
    return new Ltree(labels);
  }

  private static String randomLabel(Random r, int maxLength) {
    final char[] alphabet =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-".toCharArray();
    int length = r.nextInt(maxLength) + 1;
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(alphabet[r.nextInt(alphabet.length)]);
    }
    return sb.toString();
  }

  private static void validateLabel(CharSequence input, int offset, String label)
      throws Codec.DecodingException {
    if (label.isEmpty()) {
      throw new Codec.DecodingException(input, offset, "ltree labels must not be empty");
    }

    for (int i = 0; i < label.length(); ) {
      int codePoint = label.codePointAt(i);
      if (!Character.isLetterOrDigit(codePoint) && codePoint != '_' && codePoint != '-') {
        throw new Codec.DecodingException(input, offset, "invalid ltree label: " + label);
      }
      i += Character.charCount(codePoint);
    }
  }
}
