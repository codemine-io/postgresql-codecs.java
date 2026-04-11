package io.codemine.java.postgresql.codecs;

import com.fasterxml.jackson.databind.JsonNode;
import io.codemine.java.postgresql.CodecITBase;

public class JsonCodecIT extends CodecITBase<JsonNode> {
  public JsonCodecIT() {
    super(Codec.JSON, JsonNode.class);
  }
}
