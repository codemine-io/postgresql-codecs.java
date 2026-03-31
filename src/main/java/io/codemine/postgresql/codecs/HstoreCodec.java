package io.codemine.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/** Codec for PostgreSQL {@code hstore} values. */
final class HstoreCodec implements Codec<Hstore> {

  @Override
  public String name() {
    return "hstore";
  }

  // Extension type — OIDs are not statically known
  @Override
  public int scalarOid() {
    return 0;
  }

  @Override
  public int arrayOid() {
    return 0;
  }

  // -----------------------------------------------------------------------
  // Textual wire format
  // -----------------------------------------------------------------------
  @Override
  public void encodeInText(StringBuilder sb, Hstore value) {
    value.appendInTextTo(sb);
  }

  @Override
  public Codec.ParsingResult<Hstore> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    int pos = offset;
    int len = input.length();
    Map<String, String> map = new TreeMap<>();

    while (pos < len) {
      // Skip whitespace
      while (pos < len && Character.isWhitespace(input.charAt(pos))) {
        pos++;
      }
      if (pos >= len) {
        break;
      }
      // Parse key (must be quoted)
      if (input.charAt(pos) != '"') {
        break;
      }
      pos++; // skip opening "
      StringBuilder key = new StringBuilder();
      while (pos < len) {
        char c = input.charAt(pos);
        if (c == '\\' && pos + 1 < len) {
          key.append(input.charAt(pos + 1));
          pos += 2;
        } else if (c == '"') {
          break;
        } else {
          key.append(c);
          pos++;
        }
      }
      if (pos >= len || input.charAt(pos) != '"') {
        throw new Codec.DecodingException(input, offset, "Unterminated hstore key");
      }
      pos++; // skip closing "

      // Expect =>
      if (pos + 1 >= len || input.charAt(pos) != '=' || input.charAt(pos + 1) != '>') {
        throw new Codec.DecodingException(input, offset, "Expected '=>' in hstore");
      }
      pos += 2; // skip =>

      // Parse value: either NULL or quoted string
      String value;
      if (pos + 3 < len
          && input.charAt(pos) == 'N'
          && input.charAt(pos + 1) == 'U'
          && input.charAt(pos + 2) == 'L'
          && input.charAt(pos + 3) == 'L') {
        // Check that NULL is not followed by a quote (which would mean it's a quoted string)
        if (pos + 4 >= len || input.charAt(pos + 4) != '"') {
          value = null;
          pos += 4;
        } else {
          value = parseQuotedString(input, pos, len);
          pos = skipPastQuotedString(input, pos, len);
        }
      } else if (pos < len && input.charAt(pos) == '"') {
        value = parseQuotedString(input, pos, len);
        pos = skipPastQuotedString(input, pos, len);
      } else {
        throw new Codec.DecodingException(
            input, offset, "Expected NULL or quoted string in hstore value");
      }
      map.put(key.toString(), value);

      // Skip ", " separator
      while (pos < len && (input.charAt(pos) == ',' || input.charAt(pos) == ' ')) {
        pos++;
      }
    }
    return new Codec.ParsingResult<>(new Hstore(map), pos);
  }

  private String parseQuotedString(CharSequence input, int pos, int len)
      throws Codec.DecodingException {
    if (input.charAt(pos) != '"') {
      throw new Codec.DecodingException(input, pos, "Expected '\"' in hstore");
    }
    pos++; // skip opening "
    StringBuilder sb = new StringBuilder();
    while (pos < len) {
      char c = input.charAt(pos);
      if (c == '\\' && pos + 1 < len) {
        sb.append(input.charAt(pos + 1));
        pos += 2;
      } else if (c == '"') {
        return sb.toString();
      } else {
        sb.append(c);
        pos++;
      }
    }
    throw new Codec.DecodingException(input, "Unterminated quoted string in hstore");
  }

  private int skipPastQuotedString(CharSequence input, int pos, int len) {
    pos++; // skip opening "
    while (pos < len) {
      char c = input.charAt(pos);
      if (c == '\\' && pos + 1 < len) {
        pos += 2;
      } else if (c == '"') {
        return pos + 1; // skip closing "
      } else {
        pos++;
      }
    }
    return pos;
  }

  // -----------------------------------------------------------------------
  // Binary wire format
  // -----------------------------------------------------------------------
  @Override
  public void encodeInBinary(Hstore value, ByteArrayOutputStream out) {
    Map<String, String> entries = value.entries();
    writeInt32(out, entries.size());
    for (var entry : entries.entrySet()) {
      byte[] keyBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
      writeInt32(out, keyBytes.length);
      out.write(keyBytes, 0, keyBytes.length);
      if (entry.getValue() == null) {
        writeInt32(out, -1);
      } else {
        byte[] valueBytes = entry.getValue().getBytes(StandardCharsets.UTF_8);
        writeInt32(out, valueBytes.length);
        out.write(valueBytes, 0, valueBytes.length);
      }
    }
  }

  @Override
  public Hstore decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    int pairCount = buf.getInt();
    if (pairCount < 0) {
      throw new Codec.DecodingException("Negative pair count in hstore binary data");
    }
    Map<String, String> map = new LinkedHashMap<>(pairCount);
    for (int i = 0; i < pairCount; i++) {
      int keyLen = buf.getInt();
      if (keyLen < 0) {
        throw new Codec.DecodingException("Negative key length in hstore binary data");
      }
      byte[] keyBytes = new byte[keyLen];
      buf.get(keyBytes);
      String key = new String(keyBytes, StandardCharsets.UTF_8);

      int valueLen = buf.getInt();
      String value;
      if (valueLen == -1) {
        value = null;
      } else {
        if (valueLen < 0) {
          throw new Codec.DecodingException("Invalid value length in hstore binary data");
        }
        byte[] valueBytes = new byte[valueLen];
        buf.get(valueBytes);
        value = new String(valueBytes, StandardCharsets.UTF_8);
      }
      map.put(key, value);
    }
    return new Hstore(map);
  }

  // -----------------------------------------------------------------------
  // Random generation
  // -----------------------------------------------------------------------
  @Override
  public Hstore random(Random r, int size) {
    int numPairs = size == 0 ? 0 : r.nextInt(Math.min(size, 10) + 1);
    Map<String, String> map = new TreeMap<>();
    for (int i = 0; i < numPairs; i++) {
      String key = randomKey(r);
      String value = r.nextBoolean() ? null : randomSimpleString(r, r.nextInt(10) + 1);
      map.put(key, value);
    }
    return new Hstore(map);
  }

  private static String randomKey(Random r) {
    int len = r.nextInt(8) + 1;
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append((char) ('a' + r.nextInt(26)));
    }
    return sb.toString();
  }

  private static String randomSimpleString(Random r, int len) {
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append((char) ('a' + r.nextInt(26)));
    }
    return sb.toString();
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
