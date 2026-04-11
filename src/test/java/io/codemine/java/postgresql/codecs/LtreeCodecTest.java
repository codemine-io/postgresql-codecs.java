package io.codemine.java.postgresql.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.codemine.java.postgresql.CodecTestBase;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LtreeCodecTest extends CodecTestBase<Ltree> {
  public LtreeCodecTest() {
    super(Codec.LTREE);
  }

  // -----------------------------------------------------------------------
  // Empty ltree values
  // -----------------------------------------------------------------------

  @Test
  void emptyLtree_textEncoding() throws Exception {
    Ltree value = new Ltree(List.of());
    StringBuilder sb = new StringBuilder();
    Codec.LTREE.encodeInText(sb, value);
    assertEquals("", sb.toString());
  }

  @Test
  void emptyLtree_textRoundTrip() throws Exception {
    decodesEncodedInText(new Ltree(List.of()));
  }

  @Test
  void emptyLtree_binaryRoundTrip() throws Exception {
    decodesEncodedInBinary(new Ltree(List.of()));
  }

  @Test
  void emptyLtreeArray_textEncoding() throws Exception {
    Codec<List<Ltree>> arrayCodec = Codec.LTREE.inDim();
    StringBuilder sb = new StringBuilder();
    arrayCodec.encodeInText(sb, List.of(new Ltree(List.of())));
    assertEquals("{\"\"}", sb.toString());
  }

  @Test
  void emptyLtreeArray_textRoundTrip() throws Exception {
    decodesArrayEncodedInText(List.of(new Ltree(List.of())));
  }

  @Test
  void emptyLtreeArray_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(List.of(new Ltree(List.of())));
  }
}
