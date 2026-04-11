package io.codemine.java.postgresql.codecs;

import com.fasterxml.jackson.databind.JsonNode;
import io.codemine.java.postgresql.CodecTestBase;

public class JsonCodecTest extends CodecTestBase<JsonNode> {
  public JsonCodecTest() {
    super(Codec.JSON);
  }
}
