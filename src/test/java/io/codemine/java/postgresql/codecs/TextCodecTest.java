package io.codemine.java.postgresql.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TextCodecTest extends CodecTestBase<String> {
  public TextCodecTest() {
    super(Codec.TEXT);
  }

  // -----------------------------------------------------------------------
  // Empty string — scalar text codec
  // -----------------------------------------------------------------------

  @Test
  void emptyString_textRoundTrip() throws Exception {
    decodesEncodedInText("");
  }

  @Test
  void emptyString_binaryRoundTrip() throws Exception {
    decodesEncodedInBinary("");
  }

  // -----------------------------------------------------------------------
  // Empty array
  // -----------------------------------------------------------------------

  @Test
  void textArray_emptyArray_textRoundTrip() throws Exception {
    Codec<List<String>> arrayCodec = Codec.TEXT.inDim();
    List<String> value = List.of();
    StringBuilder sb = new StringBuilder();
    arrayCodec.encodeInText(sb, value);
    assertEquals("{}", sb.toString());
    decodesArrayEncodedInText(value);
  }

  @Test
  void textArray_emptyArray_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(List.of());
  }

  @Test
  void int4Array_emptyArray_textRoundTrip() throws Exception {
    decodesArrayEncodedInText(List.of());
  }

  @Test
  void int4Array_emptyArray_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(List.of());
  }

  @Test
  void textArray2D_emptyOuterArray_textRoundTrip() throws Exception {
    decodesArrayArrayEncodedInText(List.of());
  }

  @Test
  void textArray2D_emptyOuterArray_binaryRoundTrip() throws Exception {
    decodesArrayArrayEncodedInBinary(List.of());
  }

  @Test
  void textArray2D_outerArrayWithOneEmptyInner_textRoundTrip() throws Exception {
    decodesArrayArrayEncodedInText(List.of(List.of()));
  }

  @Test
  void textArray2D_outerArrayWithOneEmptyInner_binaryRoundTrip() throws Exception {
    decodesArrayArrayEncodedInBinary(List.of(List.of()));
  }

  // -----------------------------------------------------------------------
  // Null elements in arrays
  // -----------------------------------------------------------------------

  @Test
  void singleNull_textRoundTrip() throws Exception {
    List<String> value = Arrays.asList((String) null);
    Codec<List<String>> arrayCodec = Codec.TEXT.inDim();
    StringBuilder sb = new StringBuilder();
    arrayCodec.encodeInText(sb, value);
    assertEquals("{NULL}", sb.toString());
    decodesArrayEncodedInText(value);
  }

  @Test
  void singleNull_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(Arrays.asList((String) null));
  }

  @Test
  void nullInMiddle_textRoundTrip() throws Exception {
    List<String> value = Arrays.asList("a", null, "b");
    Codec<List<String>> arrayCodec = Codec.TEXT.inDim();
    StringBuilder sb = new StringBuilder();
    arrayCodec.encodeInText(sb, value);
    assertEquals("{a,NULL,b}", sb.toString());
    decodesArrayEncodedInText(value);
  }

  @Test
  void nullInMiddle_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(Arrays.asList("a", null, "b"));
  }

  @Test
  void nullAtStart_textRoundTrip() throws Exception {
    decodesArrayEncodedInText(Arrays.asList(null, "a", "b"));
  }

  @Test
  void nullAtStart_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(Arrays.asList(null, "a", "b"));
  }

  @Test
  void nullAtEnd_textRoundTrip() throws Exception {
    decodesArrayEncodedInText(Arrays.asList("a", "b", null));
  }

  @Test
  void nullAtEnd_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(Arrays.asList("a", "b", null));
  }

  @Test
  void allNulls_textRoundTrip() throws Exception {
    List<String> value = Arrays.asList(null, null, null);
    Codec<List<String>> arrayCodec = Codec.TEXT.inDim();
    StringBuilder sb = new StringBuilder();
    arrayCodec.encodeInText(sb, value);
    assertEquals("{NULL,NULL,NULL}", sb.toString());
    decodesArrayEncodedInText(value);
  }

  @Test
  void allNulls_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(Arrays.asList(null, null, null));
  }

  // -----------------------------------------------------------------------
  // Empty strings in arrays (distinct from null)
  // -----------------------------------------------------------------------

  @Test
  void singleEmptyString_textRoundTrip() throws Exception {
    // An empty-string element must be encoded as "" (double-quoted) to distinguish from null.
    List<String> value = List.of("");
    Codec<List<String>> arrayCodec = Codec.TEXT.inDim();
    StringBuilder sb = new StringBuilder();
    arrayCodec.encodeInText(sb, value);
    assertEquals("{\"\"}", sb.toString());
    decodesArrayEncodedInText(value);
  }

  @Test
  void singleEmptyString_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(List.of(""));
  }

  @Test
  void emptyStringInMiddle_textRoundTrip() throws Exception {
    List<String> value = List.of("a", "", "b");
    Codec<List<String>> arrayCodec = Codec.TEXT.inDim();
    StringBuilder sb = new StringBuilder();
    arrayCodec.encodeInText(sb, value);
    assertEquals("{a,\"\",b}", sb.toString());
    decodesArrayEncodedInText(value);
  }

  @Test
  void emptyStringInMiddle_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(List.of("a", "", "b"));
  }

  @Test
  void allEmptyStrings_textRoundTrip() throws Exception {
    decodesArrayEncodedInText(List.of("", "", ""));
  }

  @Test
  void allEmptyStrings_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(List.of("", "", ""));
  }

  /** The string literal {@code "NULL"} must be quoted so it round-trips as a string, not null. */
  @Test
  void nullLiteralString_textRoundTrip() throws Exception {
    List<String> value = List.of("NULL");
    Codec<List<String>> arrayCodec = Codec.TEXT.inDim();
    StringBuilder sb = new StringBuilder();
    arrayCodec.encodeInText(sb, value);
    assertEquals("{\"NULL\"}", sb.toString());
    decodesArrayEncodedInText(value);
  }

  @Test
  void nullLiteralString_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(List.of("NULL"));
  }

  /** Case-insensitive NULL variants should also be quoted. */
  @Test
  void nullLiteralStringLowercase_textRoundTrip() throws Exception {
    List<String> value = List.of("null");
    Codec<List<String>> arrayCodec = Codec.TEXT.inDim();
    StringBuilder sb = new StringBuilder();
    arrayCodec.encodeInText(sb, value);
    assertEquals("{\"null\"}", sb.toString());
    decodesArrayEncodedInText(value);
  }

  @Test
  void nullLiteralStringMixedCase_textRoundTrip() throws Exception {
    List<String> value = List.of("Null");
    Codec<List<String>> arrayCodec = Codec.TEXT.inDim();
    StringBuilder sb = new StringBuilder();
    arrayCodec.encodeInText(sb, value);
    assertEquals("{\"Null\"}", sb.toString());
    decodesArrayEncodedInText(value);
  }

  @Test
  void mixedNullAndEmptyString_textRoundTrip() throws Exception {
    decodesArrayEncodedInText(Arrays.asList(null, "", null, "a"));
  }

  @Test
  void mixedNullAndEmptyString_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(Arrays.asList(null, "", null, "a"));
  }
}
