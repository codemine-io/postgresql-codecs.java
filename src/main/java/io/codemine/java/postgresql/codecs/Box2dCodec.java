package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/** Codec for PostGIS {@code box2d} values. */
final class Box2dCodec implements Codec<Box2d> {

  @Override
  public String name() {
    return "box2d";
  }

  @Override
  public void encodeInText(StringBuilder sb, Box2d value) {
    value.appendInTextTo(sb);
  }

  @Override
  public Codec.ParsingResult<Box2d> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    String text = input.subSequence(offset, input.length()).toString().trim();
    if (!text.startsWith("BOX(") || !text.endsWith(")")) {
      throw new Codec.DecodingException(input, offset, "Invalid box2d literal: " + text);
    }
    String[] halves = text.substring(4, text.length() - 1).split(",", -1);
    if (halves.length != 2) {
      throw new Codec.DecodingException(input, offset, "Invalid box2d literal: " + text);
    }
    double[] lower = parsePair(halves[0], input, offset);
    double[] upper = parsePair(halves[1], input, offset);
    return new Codec.ParsingResult<>(
        new Box2d(lower[0], lower[1], upper[0], upper[1]), input.length());
  }

  @Override
  public void encodeInBinary(Box2d value, ByteArrayOutputStream out) {
    out.writeBytes(encodeInTextToString(value).getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Box2d decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    byte[] bytes = new byte[length];
    buf.get(bytes);
    return decodeInText(new String(bytes, StandardCharsets.UTF_8), 0).value;
  }

  @Override
  public Box2d random(Random r, int size) {
    return Box2d.of(
        randomValue(r, size), randomValue(r, size), randomValue(r, size), randomValue(r, size));
  }

  private static double[] parsePair(CharSequence text, CharSequence input, int offset)
      throws Codec.DecodingException {
    String[] parts = text.toString().trim().split("\\s+", -1);
    if (parts.length != 2) {
      throw new Codec.DecodingException(input, offset, "Invalid box2d coordinate pair: " + text);
    }
    return new double[] {Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
  }

  private static double randomValue(Random r, int size) {
    double raw = (r.nextDouble() * 2.0 - 1.0) * Math.max(size, 1);
    return Math.rint(raw * 1_000_000d) / 1_000_000d;
  }
}
