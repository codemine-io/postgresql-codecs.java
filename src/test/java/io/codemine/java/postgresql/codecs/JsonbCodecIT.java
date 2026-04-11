package io.codemine.java.postgresql.codecs;

import com.fasterxml.jackson.databind.JsonNode;
import io.codemine.java.postgresql.CodecITBase;

public class JsonbCodecIT extends CodecITBase<JsonNode> {
  public JsonbCodecIT() {
    super(Codec.JSONB, JsonNode.class);
  }
}
