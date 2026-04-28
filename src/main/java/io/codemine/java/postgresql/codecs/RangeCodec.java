package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Random;

/**
 * Codec for PostgreSQL range types. Delegates element encoding/decoding to the provided element
 * codec.
 *
 * <p>Supports all standard range types: {@code int4range}, {@code int8range}, {@code numrange},
 * {@code tsrange}, {@code tstzrange}, {@code daterange}.
 *
 * @param <A> the element type of the range
 */
final class RangeCodec<A> implements Codec<Range<A>> {

  private final Codec<A> elementCodec;
  private final Comparator<A> comparator;

  /**
   * {@code true} for discrete range types ({@code int4range}, {@code int8range}, {@code daterange})
   * that PostgreSQL canonicalises to {@code [lower, upper)}. The random generator restricts itself
   * to that canonical form so that integration tests round-trip correctly.
   */
  private final boolean discrete;

  private final String typeName;
  private final int scalarOid;
  private final int arrayOid;

  RangeCodec(
      Codec<A> elementCodec,
      Comparator<A> comparator,
      boolean discrete,
      String typeName,
      int scalarOid,
      int arrayOid) {
    this.elementCodec = elementCodec;
    this.comparator = comparator;
    this.discrete = discrete;
    this.typeName = typeName;
    this.scalarOid = scalarOid;
    this.arrayOid = arrayOid;
  }

  /** Returns the element codec. */
  Codec<A> elementCodec() {
    return elementCodec;
  }

  /** Returns the comparator used to order elements of this range. */
  Comparator<A> comparator() {
    return comparator;
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
  public void encodeInText(StringBuilder sb, Range<A> value) {
    switch (value) {
      case Range.Empty<?> e -> sb.append("empty");
      case Range.Bounded<A> b -> {
        if (b.lower() == null) {
          sb.append('(');
        } else {
          sb.append(b.lowerInclusive() ? '[' : '(');
          elementCodec.encodeInText(sb, b.lower());
        }
        sb.append(',');
        if (b.upper() == null) {
          sb.append(')');
        } else {
          elementCodec.encodeInText(sb, b.upper());
          sb.append(b.upperInclusive() ? ']' : ')');
        }
      }
    }
  }

  @Override
  public Codec.ParsingResult<Range<A>> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    if (offset >= input.length()) {
      throw new Codec.DecodingException(input, offset, "Unexpected end of range input");
    }

    // Check for "empty"
    if (input.length() - offset >= 5
        && "empty".contentEquals(input.subSequence(offset, offset + 5))) {
      return new Codec.ParsingResult<>(Range.empty(), offset + 5);
    }

    int pos = offset;
    char openBracket = input.charAt(pos++);
    if (openBracket != '[' && openBracket != '(') {
      throw new Codec.DecodingException(input, offset, "Expected '[' or '(' to open range literal");
    }
    boolean lowerInclusive = (openBracket == '[');

    // Parse lower bound value
    A lower = null;
    if (pos < input.length() && input.charAt(pos) == '"') {
      // Quoted lower bound
      pos++; // skip opening quote
      StringBuilder unquoted = new StringBuilder();
      while (pos < input.length() && input.charAt(pos) != '"') {
        if (input.charAt(pos) == '\\' && pos + 1 < input.length()) {
          unquoted.append(input.charAt(pos + 1));
          pos += 2;
        } else {
          unquoted.append(input.charAt(pos++));
        }
      }
      if (pos < input.length()) pos++; // skip closing quote
      lower = elementCodec.decodeInTextFromString(unquoted.toString());
      if (pos >= input.length() || input.charAt(pos) != ',') {
        throw new Codec.DecodingException(input, offset, "Expected ',' after lower bound");
      }
      pos++; // skip comma
    } else {
      // Unquoted: scan to comma
      int commaPos = findComma(input, pos);
      if (commaPos < 0) {
        throw new Codec.DecodingException(input, offset, "Missing ',' in range literal");
      }
      String lowerStr = input.subSequence(pos, commaPos).toString();
      if (!lowerStr.isEmpty()) {
        lower = elementCodec.decodeInTextFromString(lowerStr);
      } else {
        lowerInclusive = false; // empty text = infinite lower = always exclusive
      }
      pos = commaPos + 1;
    }

    // Parse upper bound value and closing bracket
    A upper = null;
    boolean upperInclusive;
    if (pos < input.length() && input.charAt(pos) == '"') {
      // Quoted upper bound
      pos++; // skip opening quote
      StringBuilder unquoted = new StringBuilder();
      while (pos < input.length() && input.charAt(pos) != '"') {
        if (input.charAt(pos) == '\\' && pos + 1 < input.length()) {
          unquoted.append(input.charAt(pos + 1));
          pos += 2;
        } else {
          unquoted.append(input.charAt(pos++));
        }
      }
      if (pos < input.length()) pos++; // skip closing quote
      upper = elementCodec.decodeInTextFromString(unquoted.toString());
      if (pos >= input.length()) {
        throw new Codec.DecodingException(
            input, offset, "Missing closing bracket in range literal");
      }
      char closeBracket = input.charAt(pos++);
      if (closeBracket != ']' && closeBracket != ')') {
        throw new Codec.DecodingException(
            input, offset, "Expected ']' or ')' to close range literal");
      }
      upperInclusive = (closeBracket == ']');
    } else {
      // Unquoted: scan to closing bracket
      int closePos = findCloseBracket(input, pos);
      if (closePos < 0) {
        throw new Codec.DecodingException(
            input, offset, "Missing closing bracket in range literal");
      }
      char closeBracket = input.charAt(closePos);
      upperInclusive = (closeBracket == ']');
      String upperStr = input.subSequence(pos, closePos).toString();
      if (!upperStr.isEmpty()) {
        upper = elementCodec.decodeInTextFromString(upperStr);
      } else {
        upperInclusive = false; // empty text = infinite upper = always exclusive
      }
      pos = closePos + 1;
    }

    return new Codec.ParsingResult<>(Range.of(lower, lowerInclusive, upper, upperInclusive), pos);
  }

  private static int findComma(CharSequence input, int from) {
    for (int i = from; i < input.length(); i++) {
      char c = input.charAt(i);
      if (c == '"') {
        i++;
        while (i < input.length() && input.charAt(i) != '"') {
          if (input.charAt(i) == '\\') i++;
          i++;
        }
      } else if (c == ',') {
        return i;
      }
    }
    return -1;
  }

  private static int findCloseBracket(CharSequence input, int from) {
    for (int i = from; i < input.length(); i++) {
      char c = input.charAt(i);
      if (c == '"') {
        i++;
        while (i < input.length() && input.charAt(i) != '"') {
          if (input.charAt(i) == '\\') i++;
          i++;
        }
      } else if (c == ']' || c == ')') {
        return i;
      }
    }
    return -1;
  }

  // -----------------------------------------------------------------------
  // Binary wire format
  // -----------------------------------------------------------------------

  // Flags byte layout:
  // bit 0: RANGE_EMPTY
  // bit 1: RANGE_LB_INC (lower bound inclusive)
  // bit 2: RANGE_UB_INC (upper bound inclusive)
  // bit 3: RANGE_LB_INF (lower bound infinite)
  // bit 4: RANGE_UB_INF (upper bound infinite)
  private static final int RANGE_EMPTY = 0x01;
  private static final int RANGE_LB_INC = 0x02;
  private static final int RANGE_UB_INC = 0x04;
  private static final int RANGE_LB_INF = 0x08;
  private static final int RANGE_UB_INF = 0x10;

  @Override
  public void encodeInBinary(Range<A> value, ByteArrayOutputStream out) {
    switch (value) {
      case Range.Empty<?> e -> out.write(RANGE_EMPTY);
      case Range.Bounded<A> b -> {
        int flags = 0;
        if (b.lower() == null) {
          flags |= RANGE_LB_INF;
        } else if (b.lowerInclusive()) {
          flags |= RANGE_LB_INC;
        }
        if (b.upper() == null) {
          flags |= RANGE_UB_INF;
        } else if (b.upperInclusive()) {
          flags |= RANGE_UB_INC;
        }
        out.write(flags);
        if (b.lower() != null) {
          byte[] lowerBytes = elementCodec.encodeInBinaryToBytes(b.lower());
          writeInt32(out, lowerBytes.length);
          out.write(lowerBytes, 0, lowerBytes.length);
        }
        if (b.upper() != null) {
          byte[] upperBytes = elementCodec.encodeInBinaryToBytes(b.upper());
          writeInt32(out, upperBytes.length);
          out.write(upperBytes, 0, upperBytes.length);
        }
      }
    }
  }

  @Override
  public Range<A> decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    int flags = buf.get() & 0xFF;
    if ((flags & RANGE_EMPTY) != 0) {
      return Range.empty();
    }

    boolean lowerInfinite = (flags & RANGE_LB_INF) != 0;
    boolean lowerInclusive = (flags & RANGE_LB_INC) != 0;
    boolean upperInfinite = (flags & RANGE_UB_INF) != 0;
    boolean upperInclusive = (flags & RANGE_UB_INC) != 0;

    A lower = null;
    if (!lowerInfinite) {
      int lowerLen = buf.getInt();
      lower = elementCodec.decodeInBinary(buf, lowerLen);
    }

    A upper = null;
    if (!upperInfinite) {
      int upperLen = buf.getInt();
      upper = elementCodec.decodeInBinary(buf, upperLen);
    }

    return Range.of(lower, lowerInclusive, upper, upperInclusive);
  }

  // -----------------------------------------------------------------------
  // Random generation
  // -----------------------------------------------------------------------
  @Override
  public Range<A> random(Random r, int size) {
    if (size == 0) {
      return Range.empty();
    }
    // 10% chance of empty, 10% chance of fully-unbounded, 80% bounded
    int choice = r.nextInt(10);
    if (choice == 0) {
      return Range.empty();
    }
    if (choice == 1) {
      return Range.unbounded();
    }

    boolean lowerInfinite = r.nextInt(10) == 0;
    boolean upperInfinite = r.nextInt(10) == 0;

    A lower = lowerInfinite ? null : elementCodec.random(r, size);
    A upper = upperInfinite ? null : elementCodec.random(r, size);

    // Ensure lower <= upper; if equal and we'd produce an empty range, fall back to empty.
    if (!lowerInfinite && !upperInfinite) {
      int cmp = comparator.compare(lower, upper);
      if (cmp == 0) {
        return Range.empty();
      }
      if (cmp > 0) {
        // Swap so that lower < upper.
        A tmp = lower;
        lower = upper;
        upper = tmp;
      }
    }

    if (discrete) {
      // Discrete types are canonicalised by PostgreSQL to [lower, upper), so always use that form
      // to avoid round-trip mismatches in integration tests.
      return Range.bounded(lower, upper);
    }

    // For continuous types all four inclusivity combinations are valid and distinct.
    boolean lowerInclusive = !lowerInfinite && r.nextBoolean();
    boolean upperInclusive = !upperInfinite && r.nextBoolean();
    return Range.of(lower, lowerInclusive, upper, upperInclusive);
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
