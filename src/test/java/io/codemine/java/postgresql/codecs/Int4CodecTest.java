package io.codemine.java.postgresql.codecs;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class Int4CodecTest extends CodecTestBase<Integer> {
  public Int4CodecTest() {
    super(Codec.INT4);
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
  void int4ArrayWithNulls_textRoundTrip() throws Exception {
    decodesArrayEncodedInText(Arrays.asList(1, null, 3));
  }

  @Test
  void int4ArrayWithNulls_binaryRoundTrip() throws Exception {
    decodesArrayEncodedInBinary(Arrays.asList(1, null, 3));
  }
}
