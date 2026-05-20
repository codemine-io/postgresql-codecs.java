package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/** Codec for PostGIS {@code box3d} values. */
final class Box3dCodec implements Codec<Box3d> {

  @Override
  public String name() {
    return "box3d";
  }

  @Override
  public void encodeInText(StringBuilder sb, Box3d value) {
    value.appendInTextTo(sb);
  }

  @Override
  public Codec.ParsingResult<Box3d> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    String text = input.subSequence(offset, input.length()).toString().trim();
    if (!text.startsWith("BOX3D(") || !text.endsWith(")")) {
      throw new Codec.DecodingException(input, offset, "Invalid box3d literal: " + text);
    }
    String[] halves = text.substring(6, text.length() - 1).split(",", -1);
    if (halves.length != 2) {
      throw new Codec.DecodingException(input, offset, "Invalid box3d literal: " + text);
    }
    double[] lower = parseTriple(halves[0], input, offset);
    double[] upper = parseTriple(halves[1], input, offset);
    return new Codec.ParsingResult<>(
        new Box3d(lower[0], lower[1], lower[2], upper[0], upper[1], upper[2]), input.length());
  }

  @Override
  public void encodeInBinary(Box3d value, ByteArrayOutputStream out) {
    out.writeBytes(encodeInTextToString(value).getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Box3d decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    byte[] bytes = new byte[length];
    buf.get(bytes);
    return decodeInText(new String(bytes, StandardCharsets.UTF_8), 0).value;
  }

  @Override
  public Box3d random(Random r, int size) {
    return Box3d.of(
        randomValue(r, size),
        randomValue(r, size),
        randomValue(r, size),
        randomValue(r, size),
        randomValue(r, size),
        randomValue(r, size));
  }

  private static double[] parseTriple(CharSequence text, CharSequence input, int offset)
      throws Codec.DecodingException {
    String[] parts = text.toString().trim().split("\\s+", -1);
    if (parts.length != 3) {
      throw new Codec.DecodingException(input, offset, "Invalid box3d coordinate triple: " + text);
    }
    return new double[] {
      Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2])
    };
  }

  private static double randomValue(Random r, int size) {
    double raw = (r.nextDouble() * 2.0 - 1.0) * Math.max(size, 1);
    return Math.rint(raw * 1_000_000d) / 1_000_000d;
  }
}
