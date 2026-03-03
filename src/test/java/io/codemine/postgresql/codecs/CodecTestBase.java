package io.codemine.postgresql.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.util.List;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

abstract class CodecTestBase<A> {

  private final Codec<A> codec;
  private final Class<A> type;
  private final Codec<List<A>> arrayCodec;

  @SuppressWarnings("unchecked")
  protected CodecTestBase(Codec<A> codec, Class<A> type) {
    this.codec = codec;
    this.type = type;
    this.arrayCodec = codec.inDim();
  }

  @Provide
  Arbitrary<A> values() {
    return net.jqwik.api.Arbitraries.randomValue(codec::random);
  }

  @Provide
  Arbitrary<List<A>> arrayValues() {
    return net.jqwik.api.Arbitraries.randomValue(arrayCodec::random);
  }

  @Property(tries = 100)
  void decodesEncodedInBinary(@ForAll("values") A value) throws Exception {
    byte[] encoded = codec.encodeInBinaryAsByteArray(value);
    A decoded = codec.decodeInBinary(ByteBuffer.wrap(encoded), encoded.length);
    assertEquals(value, decoded);
  }

  @Property(tries = 100)
  void decodesEncodedInText(@ForAll("values") A value) throws Exception {
    StringBuilder sb = new StringBuilder();
    codec.write(sb, value);
    String encoded = sb.toString();
    A decoded = codec.parse(encoded, 0).value;
    assertEquals(value, decoded);
  }
}
