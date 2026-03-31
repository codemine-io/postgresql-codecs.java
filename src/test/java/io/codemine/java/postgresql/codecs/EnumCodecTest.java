package io.codemine.java.postgresql.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link EnumCodec}, covering both text and binary round-trips. */
public class EnumCodecTest extends CodecTestBase<EnumCodecTest.Color> {

  // -----------------------------------------------------------------------
  // Test enum and codec
  // -----------------------------------------------------------------------
  enum Color {
    RED,
    GREEN,
    BLUE
  }

  static final EnumCodec<Color> COLOR_CODEC =
      new EnumCodec<>(
          "", "color", Map.of(Color.RED, "red", Color.GREEN, "green", Color.BLUE, "blue"));

  public EnumCodecTest() {
    super(COLOR_CODEC);
  }

  // -----------------------------------------------------------------------
  // Label-specific assertions
  // -----------------------------------------------------------------------
  @Test
  void writesCorrectLabels() throws Exception {
    assertEquals("red", writeToString(Color.RED));
    assertEquals("green", writeToString(Color.GREEN));
    assertEquals("blue", writeToString(Color.BLUE));
  }

  @Test
  void parsesCorrectLabels() throws Exception {
    assertEquals(Color.RED, COLOR_CODEC.decodeInText("red", 0).value);
    assertEquals(Color.GREEN, COLOR_CODEC.decodeInText("green", 0).value);
    assertEquals(Color.BLUE, COLOR_CODEC.decodeInText("blue", 0).value);
  }

  @Test
  void parseThrowsOnUnknownLabel() {
    assertThrows(Codec.DecodingException.class, () -> COLOR_CODEC.decodeInText("yellow", 0));
  }

  @Test
  void typeSig() {
    assertEquals("color", COLOR_CODEC.typeSig());
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------
  private String writeToString(Color value) {
    StringBuilder sb = new StringBuilder();
    COLOR_CODEC.encodeInText(sb, value);
    return sb.toString();
  }
}
