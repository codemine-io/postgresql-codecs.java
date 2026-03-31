package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Codec for PostgreSQL multirange types. Delegates range encoding/decoding to the provided range
 * codec.
 *
 * @param <A> the element type of the ranges within the multirange
 */
final class MultirangeCodec<A> implements Codec<Multirange<A>> {

  private final Codec<Range<A>> rangeCodec;
  private final String typeName;
  private final int scalarOid;
  private final int arrayOid;

  MultirangeCodec(Codec<Range<A>> rangeCodec, String typeName, int scalarOid, int arrayOid) {
    this.rangeCodec = rangeCodec;
    this.typeName = typeName;
    this.scalarOid = scalarOid;
    this.arrayOid = arrayOid;
  }

  @Override
  public String name() {
    return typeName;
  }

  @Override
  public int scalarOid() {
    return scalarOid;
  }

  @Override
  public int arrayOid() {
    return arrayOid;
  }

  // -----------------------------------------------------------------------
  // Textual wire format
  // -----------------------------------------------------------------------
  @Override
  public void encodeInText(StringBuilder sb, Multirange<A> value) {
    sb.append('{');
    for (int i = 0; i < value.ranges().size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      rangeCodec.encodeInText(sb, value.ranges().get(i));
    }
    sb.append('}');
  }

  @Override
  public Codec.ParsingResult<Multirange<A>> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    if (offset >= input.length() || input.charAt(offset) != '{') {
      throw new Codec.DecodingException(input, offset, "Expected '{' to open multirange literal");
    }
    int pos = offset + 1;
    // Skip whitespace
    while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
      pos++;
    }
    List<Range<A>> ranges = new ArrayList<>();
    if (pos < input.length() && input.charAt(pos) == '}') {
      return new Codec.ParsingResult<>(new Multirange<>(ranges), pos + 1);
    }
    while (pos < input.length()) {
      // Skip whitespace
      while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
        pos++;
      }
      if (pos >= input.length()) {
        break;
      }
      if (input.charAt(pos) == '}') {
        return new Codec.ParsingResult<>(new Multirange<>(ranges), pos + 1);
      }
      Codec.ParsingResult<Range<A>> result = rangeCodec.decodeInText(input, pos);
      ranges.add(result.value);
      pos = result.nextOffset;
      // Skip comma separator
      while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
        pos++;
      }
      if (pos < input.length() && input.charAt(pos) == ',') {
        pos++;
      }
    }
    throw new Codec.DecodingException(input, offset, "Unexpected end of input parsing multirange");
  }

  // -----------------------------------------------------------------------
  // Binary wire format
  // -----------------------------------------------------------------------
  @Override
  public void encodeInBinary(Multirange<A> value, ByteArrayOutputStream out) {
    // 4 bytes: number of ranges
    writeInt32(out, value.ranges().size());
    for (Range<A> range : value.ranges()) {
      byte[] rangeBytes = rangeCodec.encodeInBinaryToBytes(range);
      writeInt32(out, rangeBytes.length);
      out.write(rangeBytes, 0, rangeBytes.length);
    }
  }

  @Override
  public Multirange<A> decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    int numRanges = buf.getInt();
    if (numRanges < 0) {
      throw new Codec.DecodingException("Negative range count in multirange binary data");
    }
    List<Range<A>> ranges = new ArrayList<>(numRanges);
    for (int i = 0; i < numRanges; i++) {
      int rangeLen = buf.getInt();
      ranges.add(rangeCodec.decodeInBinary(buf, rangeLen));
    }
    return new Multirange<>(ranges);
  }

  // -----------------------------------------------------------------------
  // Random generation
  // -----------------------------------------------------------------------
  @Override
  public Multirange<A> random(Random r, int size) {
    if (size == 0) {
      return new Multirange<>(List.of());
    }
    int numRanges = r.nextInt(Math.min(size, 5) + 1);
    List<Range<A>> ranges = new ArrayList<>(numRanges);
    for (int i = 0; i < numRanges; i++) {
      ranges.add(rangeCodec.random(r, size));
    }
    return new Multirange<>(ranges);
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------
  private static void writeInt32(ByteArrayOutputStream out, int v) {
    out.write((v >>> 24) & 0xFF);
    out.write((v >>> 16) & 0xFF);
    out.write((v >>> 8) & 0xFF);
    out.write(v & 0xFF);
  }
}
