package io.codemine.java.postgresql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.codemine.java.postgresql.codecs.Codec;
import java.nio.ByteBuffer;
import java.util.List;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

public abstract class CodecTestBase<A> {

  private final Codec<A> codec;
  private final Codec<List<A>> arrayCodec;
  private final Codec<List<List<A>>> arrayArrayCodec;

  @SuppressWarnings("unchecked")
  protected CodecTestBase(Codec<A> codec) {
    this.codec = codec;
    this.arrayCodec = codec.inDim();
    this.arrayArrayCodec = arrayCodec.inDim();
  }

  @Provide
  Arbitrary<A> values() {
    return net.jqwik.api.Arbitraries.fromGeneratorWithSize(
        size -> r -> net.jqwik.api.Shrinkable.unshrinkable(codec.random(r, size)));
  }

  @Provide
  Arbitrary<List<A>> arrayValues() {
    return net.jqwik.api.Arbitraries.fromGeneratorWithSize(
        size -> r -> net.jqwik.api.Shrinkable.unshrinkable(arrayCodec.random(r, size)));
  }

  @Provide
  Arbitrary<List<List<A>>> arrayArrayValues() {
    return net.jqwik.api.Arbitraries.fromGeneratorWithSize(
        size -> r -> net.jqwik.api.Shrinkable.unshrinkable(arrayArrayCodec.random(r, size)));
  }

  @Property(tries = 100)
  protected void decodesEncodedInBinary(@ForAll("values") A value) throws Exception {
    byte[] encoded = codec.encodeInBinaryToBytes(value);
    A decoded = codec.decodeInBinary(ByteBuffer.wrap(encoded), encoded.length);
    assertEquals(value, decoded);
  }

  @Property(tries = 100)
  protected void decodesEncodedInText(@ForAll("values") A value) throws Exception {
    StringBuilder sb = new StringBuilder();
    codec.encodeInText(sb, value);
    String encoded = sb.toString();
    A decoded = codec.decodeInText(encoded, 0).value;
    assertEquals(value, decoded);
  }

  @Property(tries = 100)
  protected void decodesArrayEncodedInBinary(@ForAll("arrayValues") List<A> value)
      throws Exception {
    byte[] encoded = arrayCodec.encodeInBinaryToBytes(value);
    List<A> decoded = arrayCodec.decodeInBinary(ByteBuffer.wrap(encoded), encoded.length);
    assertEquals(value, decoded);
  }

  @Property(tries = 100)
  protected void decodesArrayEncodedInText(@ForAll("arrayValues") List<A> value) throws Exception {
    StringBuilder sb = new StringBuilder();
    arrayCodec.encodeInText(sb, value);
    String encoded = sb.toString();
    List<A> decoded = arrayCodec.decodeInText(encoded, 0).value;
    assertEquals(value, decoded);
  }

  @Property(tries = 100)
  protected void decodesArrayArrayEncodedInBinary(@ForAll("arrayArrayValues") List<List<A>> value)
      throws Exception {
    byte[] encoded = arrayArrayCodec.encodeInBinaryToBytes(value);
    List<List<A>> decoded =
        arrayArrayCodec.decodeInBinary(ByteBuffer.wrap(encoded), encoded.length);
    assertEquals(value, decoded);
  }

  @Property(tries = 100)
  protected void decodesArrayArrayEncodedInText(@ForAll("arrayArrayValues") List<List<A>> value)
      throws Exception {
    StringBuilder sb = new StringBuilder();
    arrayArrayCodec.encodeInText(sb, value);
    String encoded = sb.toString();
    List<List<A>> decoded = arrayArrayCodec.decodeInText(encoded, 0).value;
    assertEquals(value, decoded);
  }
}
