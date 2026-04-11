package io.codemine.java.postgresql.codecs;

import com.fasterxml.jackson.databind.JsonNode;
import io.codemine.java.postgresql.CodecTestBase;

public class JsonbCodecTest extends CodecTestBase<JsonNode> {
  public JsonbCodecTest() {
    super(Codec.JSONB);
  }
}
