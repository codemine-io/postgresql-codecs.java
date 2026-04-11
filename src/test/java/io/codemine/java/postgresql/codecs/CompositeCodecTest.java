package io.codemine.java.postgresql.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.codemine.java.postgresql.CodecTestBase;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import net.jqwik.api.Group;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CompositeCodec}, covering text and binary round-trips for five composite
 * scenarios:
 *
 * <ol>
 *   <li>{@link SingleValueTests} – 1-field scalar composite {@code (value int4)}
 *   <li>{@link SimplePointTests} – 2-field scalar composite {@code (x int4, y int4)}
 *   <li>{@link NestedSegmentTests} – composite containing two composites {@code (start Point, end
 *       Point)}
 *   <li>{@link CompositeWithArrayTests} – composite with an array field {@code (tag text, items
 *       text[])}
 *   <li>{@link NestedCompositeWithArrayTests} – composite containing a composite plus an array
 *       {@code (label text, seg Segment, tags text[])}
 * </ol>
 *
 * <p>Additional explicit cases cover nullable and empty fields in composite values.
 */
class CompositeCodecTest {

  // -----------------------------------------------------------------------
  // Test value types (records)
  // -----------------------------------------------------------------------

  record Point(int x, int y) {}

  record SingleValue(int value) {}

  record Segment(Point start, Point end) {}

  record TaggedData(String tag, List<String> items) {}

  record AnnotatedSegment(String label, Segment seg, List<String> tags) {}

  record Sextuple(int a, int b, int c, int d, int e, int f) {}

  // -----------------------------------------------------------------------
  // Composite codecs
  // -----------------------------------------------------------------------

  static final CompositeCodec<SingleValue> SINGLE_VALUE_CODEC =
      new CompositeCodec<>(
          "",
          "test_single_value",
          (Integer value) -> new SingleValue(value),
          new CompositeCodec.Field<>("value", SingleValue::value, Codec.INT4));

  static final CompositeCodec<Point> POINT_CODEC =
      new CompositeCodec<>(
          "",
          "test_pt",
          (Integer x) -> (Integer y) -> new Point(x, y),
          new CompositeCodec.Field<>("x", Point::x, Codec.INT4),
          new CompositeCodec.Field<>("y", Point::y, Codec.INT4));

  static final CompositeCodec<Segment> SEGMENT_CODEC =
      new CompositeCodec<>(
          "",
          "test_seg",
          (Point start) -> (Point end) -> new Segment(start, end),
          new CompositeCodec.Field<>("start_pt", Segment::start, POINT_CODEC),
          new CompositeCodec.Field<>("end_pt", Segment::end, POINT_CODEC));

  static final CompositeCodec<TaggedData> TAGGED_DATA_CODEC =
      new CompositeCodec<>(
          "",
          "test_tagged",
          (String tag) -> (List<String> items) -> new TaggedData(tag, items),
          new CompositeCodec.Field<>("tag", TaggedData::tag, Codec.TEXT),
          new CompositeCodec.Field<>("items", TaggedData::items, Codec.TEXT.inDim()));

  static final CompositeCodec<AnnotatedSegment> ANNOTATED_CODEC =
      new CompositeCodec<>(
          "",
          "test_ann_seg",
          (String label) ->
              (Segment seg) -> (List<String> tags) -> new AnnotatedSegment(label, seg, tags),
          new CompositeCodec.Field<>("label", AnnotatedSegment::label, Codec.TEXT),
          new CompositeCodec.Field<>("seg", AnnotatedSegment::seg, SEGMENT_CODEC),
          new CompositeCodec.Field<>("tags", AnnotatedSegment::tags, Codec.TEXT.inDim()));

  static final CompositeCodec<Sextuple> SEXTUPLE_TYPED_CODEC =
      new CompositeCodec<>(
          "",
          "test_sextuple",
          (Integer a) ->
              (Integer b) ->
                  (Integer c) ->
                      (Integer d) -> (Integer e) -> (Integer f) -> new Sextuple(a, b, c, d, e, f),
          new CompositeCodec.Field<>("a", Sextuple::a, Codec.INT4),
          new CompositeCodec.Field<>("b", Sextuple::b, Codec.INT4),
          new CompositeCodec.Field<>("c", Sextuple::c, Codec.INT4),
          new CompositeCodec.Field<>("d", Sextuple::d, Codec.INT4),
          new CompositeCodec.Field<>("e", Sextuple::e, Codec.INT4),
          new CompositeCodec.Field<>("f", Sextuple::f, Codec.INT4));

  static final CompositeCodec<Sextuple> SEXTUPLE_VARARG_CODEC =
      new CompositeCodec<>(
          "",
          "test_sextuple",
          (Object[] a) ->
              new Sextuple(
                  (Integer) a[0],
                  (Integer) a[1],
                  (Integer) a[2],
                  (Integer) a[3],
                  (Integer) a[4],
                  (Integer) a[5]),
          new CompositeCodec.Field<>("a", Sextuple::a, Codec.INT4),
          new CompositeCodec.Field<>("b", Sextuple::b, Codec.INT4),
          new CompositeCodec.Field<>("c", Sextuple::c, Codec.INT4),
          new CompositeCodec.Field<>("d", Sextuple::d, Codec.INT4),
          new CompositeCodec.Field<>("e", Sextuple::e, Codec.INT4),
          new CompositeCodec.Field<>("f", Sextuple::f, Codec.INT4));

  // -----------------------------------------------------------------------
  // Edge cases — null and empty fields in composites
  // -----------------------------------------------------------------------

  record NullableTextPair(String first, String second) {}

  static final CompositeCodec<NullableTextPair> NULLABLE_TEXT_PAIR_CODEC =
      new CompositeCodec<>(
          "",
          "nullable_text_pair",
          (String first) -> (String second) -> new NullableTextPair(first, second),
          new CompositeCodec.Field<>("first", NullableTextPair::first, Codec.TEXT),
          new CompositeCodec.Field<>("second", NullableTextPair::second, Codec.TEXT));

  record NullableTaggedData(String tag, List<String> items) {}

  static final CompositeCodec<NullableTaggedData> NULLABLE_TAGGED_CODEC =
      new CompositeCodec<>(
          "",
          "nullable_tagged",
          (String tag) -> (List<String> items) -> new NullableTaggedData(tag, items),
          new CompositeCodec.Field<>("tag", NullableTaggedData::tag, Codec.TEXT),
          new CompositeCodec.Field<>("items", NullableTaggedData::items, Codec.TEXT.inDim()));

  record Triple(String a, String b, String c) {}

  static final CompositeCodec<Triple> TRIPLE_CODEC =
      new CompositeCodec<>(
          "",
          "triple",
          (String a) -> (String b) -> (String c) -> new Triple(a, b, c),
          new CompositeCodec.Field<>("a", Triple::a, Codec.TEXT),
          new CompositeCodec.Field<>("b", Triple::b, Codec.TEXT),
          new CompositeCodec.Field<>("c", Triple::c, Codec.TEXT));

  private static <A> void assertTextRoundTrip(Codec<A> codec, A value) throws Exception {
    StringBuilder sb = new StringBuilder();
    codec.encodeInText(sb, value);
    String encoded = sb.toString();
    A decoded = codec.decodeInText(encoded, 0).value;
    assertEquals(value, decoded);
  }

  private static <A> void assertBinaryRoundTrip(Codec<A> codec, A value) throws Exception {
    byte[] encoded = codec.encodeInBinaryToBytes(value);
    A decoded = codec.decodeInBinary(ByteBuffer.wrap(encoded), encoded.length);
    assertEquals(value, decoded);
  }

  @Test
  void binaryDecodeRejectsUnexpectedScalarFieldOid() {
    Point value = new Point(1, 2);
    byte[] encoded = POINT_CODEC.encodeInBinaryToBytes(value);
    ByteBuffer.wrap(encoded).putInt(Integer.BYTES, Codec.TEXT.oid());

    var error =
        assertThrows(
            Codec.DecodingException.class,
            () -> POINT_CODEC.decodeInBinary(ByteBuffer.wrap(encoded), encoded.length));

    assertEquals(
        "Unexpected field OID in composite binary decode for field 'x' of test_pt: expected "
            + Codec.INT4.oid()
            + ", got "
            + Codec.TEXT.oid(),
        error.getMessage());
  }

  @Test
  void binaryEncodeUsesArrayOidForArrayField() {
    TaggedData value = new TaggedData("tag", List.of("alpha"));
    ByteBuffer buf = ByteBuffer.wrap(TAGGED_DATA_CODEC.encodeInBinaryToBytes(value));

    assertEquals(2, buf.getInt());
    buf.getInt();
    int firstFieldLength = buf.getInt();
    buf.position(buf.position() + firstFieldLength);

    assertEquals(Codec.TEXT.inDim().oid(), buf.getInt());
  }

  @Test
  void binaryDecodeAcceptsNonzeroFieldOidWhenTypeOidIsUnknown() throws Exception {
    Segment value = new Segment(new Point(1, 2), new Point(3, 4));
    byte[] encoded = SEGMENT_CODEC.encodeInBinaryToBytes(value);
    int serverAssignedOid = 12_345;
    ByteBuffer.wrap(encoded).putInt(Integer.BYTES, serverAssignedOid);

    assertEquals(value, SEGMENT_CODEC.decodeInBinary(ByteBuffer.wrap(encoded), encoded.length));
  }

  // -----------------------------------------------------------------------
  // Test groups — each extends CodecTestBase to get the full binary+text
  // round-trip property suite.
  // -----------------------------------------------------------------------

  /** 1-field scalar composite: {@code (value int4)}. */
  @Group
  class SingleValueTests extends CodecTestBase<SingleValue> {
    SingleValueTests() {
      super(SINGLE_VALUE_CODEC);
    }
  }

  /** 2-field scalar composite: {@code (x int4, y int4)}. */
  @Group
  class SimplePointTests extends CodecTestBase<Point> {
    SimplePointTests() {
      super(POINT_CODEC);
    }
  }

  /**
   * Composite whose fields are themselves composites: {@code (start test_pt, end test_pt)}.
   * Exercises nested composite encoding in both text and binary formats.
   */
  @Group
  class NestedSegmentTests extends CodecTestBase<Segment> {
    NestedSegmentTests() {
      super(SEGMENT_CODEC);
    }
  }

  /**
   * Composite with a scalar field and a 1-D array field: {@code (tag text, items text[])}.
   * Exercises array-within-composite encoding in both text and binary formats.
   */
  @Group
  class CompositeWithArrayTests extends CodecTestBase<TaggedData> {
    CompositeWithArrayTests() {
      super(TAGGED_DATA_CODEC);
    }
  }

  /**
   * Composite that nests another composite plus a 1-D array: {@code (label text, seg test_seg, tags
   * text[])}. Exercises the combination of nested composites and arrays.
   */
  @Group
  class NestedCompositeWithArrayTests extends CodecTestBase<AnnotatedSegment> {
    NestedCompositeWithArrayTests() {
      super(ANNOTATED_CODEC);
    }
  }

  /** 6-field scalar composite using the arity-6 typed constructor. */
  @Group
  class SextupleTypedTests extends CodecTestBase<Sextuple> {
    SextupleTypedTests() {
      super(SEXTUPLE_TYPED_CODEC);
    }
  }

  /** 6-field scalar composite using the vararg constructor. */
  @Group
  class SextupleVarargTests extends CodecTestBase<Sextuple> {
    SextupleVarargTests() {
      super(SEXTUPLE_VARARG_CODEC);
    }
  }

  // -----------------------------------------------------------------------
  // Null fields in composite types
  // -----------------------------------------------------------------------

  @Test
  void nullableTextPair_nullFirstField_textEncoding() throws Exception {
    NullableTextPair value = new NullableTextPair(null, "hello");
    StringBuilder sb = new StringBuilder();
    NULLABLE_TEXT_PAIR_CODEC.encodeInText(sb, value);
    assertEquals("(,hello)", sb.toString());
  }

  @Test
  void nullableTextPair_nullFirstField_textRoundTrip() throws Exception {
    assertTextRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair(null, "hello"));
  }

  @Test
  void nullableTextPair_nullFirstField_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair(null, "hello"));
  }

  @Test
  void nullableTextPair_nullLastField_textEncoding() throws Exception {
    NullableTextPair value = new NullableTextPair("hello", null);
    StringBuilder sb = new StringBuilder();
    NULLABLE_TEXT_PAIR_CODEC.encodeInText(sb, value);
    assertEquals("(hello,)", sb.toString());
  }

  @Test
  void nullableTextPair_nullLastField_textRoundTrip() throws Exception {
    assertTextRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair("hello", null));
  }

  @Test
  void nullableTextPair_nullLastField_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair("hello", null));
  }

  @Test
  void nullableTextPair_allNullFields_textEncoding() throws Exception {
    NullableTextPair value = new NullableTextPair(null, null);
    StringBuilder sb = new StringBuilder();
    NULLABLE_TEXT_PAIR_CODEC.encodeInText(sb, value);
    assertEquals("(,)", sb.toString());
  }

  @Test
  void nullableTextPair_allNullFields_textRoundTrip() throws Exception {
    assertTextRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair(null, null));
  }

  @Test
  void nullableTextPair_allNullFields_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair(null, null));
  }

  // -----------------------------------------------------------------------
  // Empty string fields in composite types (distinct from null)
  // -----------------------------------------------------------------------

  @Test
  void nullableTextPair_emptyStringFirstField_textEncoding() throws Exception {
    NullableTextPair value = new NullableTextPair("", "hello");
    StringBuilder sb = new StringBuilder();
    NULLABLE_TEXT_PAIR_CODEC.encodeInText(sb, value);
    // An empty text field must be encoded as "" to distinguish it from NULL.
    assertEquals("(\"\",hello)", sb.toString());
  }

  @Test
  void nullableTextPair_emptyStringFirstField_textRoundTrip() throws Exception {
    assertTextRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair("", "hello"));
  }

  @Test
  void nullableTextPair_emptyStringFirstField_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair("", "hello"));
  }

  @Test
  void nullableTextPair_emptyStringLastField_textEncoding() throws Exception {
    NullableTextPair value = new NullableTextPair("hello", "");
    StringBuilder sb = new StringBuilder();
    NULLABLE_TEXT_PAIR_CODEC.encodeInText(sb, value);
    assertEquals("(hello,\"\")", sb.toString());
  }

  @Test
  void nullableTextPair_emptyStringLastField_textRoundTrip() throws Exception {
    assertTextRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair("hello", ""));
  }

  @Test
  void nullableTextPair_emptyStringLastField_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair("hello", ""));
  }

  @Test
  void nullableTextPair_allEmptyStringFields_textEncoding() throws Exception {
    NullableTextPair value = new NullableTextPair("", "");
    StringBuilder sb = new StringBuilder();
    NULLABLE_TEXT_PAIR_CODEC.encodeInText(sb, value);
    assertEquals("(\"\",\"\")", sb.toString());
  }

  @Test
  void nullableTextPair_allEmptyStringFields_textRoundTrip() throws Exception {
    assertTextRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair("", ""));
  }

  @Test
  void nullableTextPair_allEmptyStringFields_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(NULLABLE_TEXT_PAIR_CODEC, new NullableTextPair("", ""));
  }

  // -----------------------------------------------------------------------
  // Null / empty array fields inside composite types
  // -----------------------------------------------------------------------

  @Test
  void nullableTaggedData_nullArrayField_textEncoding() throws Exception {
    NullableTaggedData value = new NullableTaggedData("hello", null);
    StringBuilder sb = new StringBuilder();
    NULLABLE_TAGGED_CODEC.encodeInText(sb, value);
    assertEquals("(hello,)", sb.toString());
  }

  @Test
  void nullableTaggedData_nullArrayField_textRoundTrip() throws Exception {
    assertTextRoundTrip(NULLABLE_TAGGED_CODEC, new NullableTaggedData("hello", null));
  }

  @Test
  void nullableTaggedData_nullArrayField_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(NULLABLE_TAGGED_CODEC, new NullableTaggedData("hello", null));
  }

  @Test
  void nullableTaggedData_emptyArrayField_textRoundTrip() throws Exception {
    assertTextRoundTrip(NULLABLE_TAGGED_CODEC, new NullableTaggedData("hello", List.of()));
  }

  @Test
  void nullableTaggedData_emptyArrayField_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(NULLABLE_TAGGED_CODEC, new NullableTaggedData("hello", List.of()));
  }

  @Test
  void nullableTaggedData_arrayFieldWithNullElements_textRoundTrip() throws Exception {
    assertTextRoundTrip(
        NULLABLE_TAGGED_CODEC, new NullableTaggedData("hello", Arrays.asList("a", null, "b")));
  }

  @Test
  void nullableTaggedData_arrayFieldWithNullElements_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(
        NULLABLE_TAGGED_CODEC, new NullableTaggedData("hello", Arrays.asList("a", null, "b")));
  }

  @Test
  void nullableTaggedData_arrayFieldWithEmptyStrings_textRoundTrip() throws Exception {
    assertTextRoundTrip(
        NULLABLE_TAGGED_CODEC, new NullableTaggedData("hello", List.of("", "x", "")));
  }

  @Test
  void nullableTaggedData_arrayFieldWithEmptyStrings_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(
        NULLABLE_TAGGED_CODEC, new NullableTaggedData("hello", List.of("", "x", "")));
  }

  @Test
  void nullableTaggedData_allNullFields_textRoundTrip() throws Exception {
    assertTextRoundTrip(NULLABLE_TAGGED_CODEC, new NullableTaggedData(null, null));
  }

  @Test
  void nullableTaggedData_allNullFields_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(NULLABLE_TAGGED_CODEC, new NullableTaggedData(null, null));
  }

  @Test
  void nullableTaggedData_nullTagWithEmptyArray_textRoundTrip() throws Exception {
    assertTextRoundTrip(NULLABLE_TAGGED_CODEC, new NullableTaggedData(null, List.of()));
  }

  @Test
  void nullableTaggedData_nullTagWithEmptyArray_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(NULLABLE_TAGGED_CODEC, new NullableTaggedData(null, List.of()));
  }

  // -----------------------------------------------------------------------
  // 3-field composite with null in the middle
  // -----------------------------------------------------------------------

  @Test
  void triple_nullMiddleField_textEncoding() throws Exception {
    Triple value = new Triple("first", null, "third");
    StringBuilder sb = new StringBuilder();
    TRIPLE_CODEC.encodeInText(sb, value);
    assertEquals("(first,,third)", sb.toString());
  }

  @Test
  void triple_nullMiddleField_textRoundTrip() throws Exception {
    assertTextRoundTrip(TRIPLE_CODEC, new Triple("first", null, "third"));
  }

  @Test
  void triple_nullMiddleField_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(TRIPLE_CODEC, new Triple("first", null, "third"));
  }

  @Test
  void triple_allNulls_textEncoding() throws Exception {
    Triple value = new Triple(null, null, null);
    StringBuilder sb = new StringBuilder();
    TRIPLE_CODEC.encodeInText(sb, value);
    assertEquals("(,,)", sb.toString());
  }

  @Test
  void triple_allNulls_textRoundTrip() throws Exception {
    assertTextRoundTrip(TRIPLE_CODEC, new Triple(null, null, null));
  }

  @Test
  void triple_allNulls_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(TRIPLE_CODEC, new Triple(null, null, null));
  }

  @Test
  void triple_mixedNullsAndEmptyStrings_textRoundTrip() throws Exception {
    assertTextRoundTrip(TRIPLE_CODEC, new Triple(null, "", null));
  }

  @Test
  void triple_mixedNullsAndEmptyStrings_binaryRoundTrip() throws Exception {
    assertBinaryRoundTrip(TRIPLE_CODEC, new Triple(null, "", null));
  }
}
