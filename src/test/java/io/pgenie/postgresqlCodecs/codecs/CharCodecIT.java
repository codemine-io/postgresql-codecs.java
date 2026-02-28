package io.pgenie.postgresqlCodecs.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class CharCodecIT extends CodecITBase {

    @Test
    void charRoundTrip() throws Exception {
        assertEquals("a", roundTrip(Codec.CHAR, "a"));
    }
}
