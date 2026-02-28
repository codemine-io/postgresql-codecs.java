package io.pgenie.postgresqlCodecs.codecs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class TsvectorCodecIT extends CodecITBase {

    @Test
    void tsvectorRoundTrip() throws Exception {
        // Note: PostgreSQL normalizes tsvectors
        String text = roundTripText(Codec.TSVECTOR, "tsvector", "'hello' 'world'");
        assertNotNull(text);
        assertTrue(text.contains("hello") && text.contains("world"));
    }

    @Test
    void tsvectorNull() throws Exception {
        assertNull(roundTripText(Codec.TSVECTOR, "tsvector", null));
    }

    /**
     * Property: arbitrary canonical tsvectors are accepted by PostgreSQL.
     *
     * <p>The generator produces pre-sorted, deduplicated, lowercase lexemes in
     * single-quoted form. PostgreSQL should return them in the same canonical
     * format.
     */
    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#tsvectors")
    void tsvectorPropertyRoundTrip(String value) throws Exception {
        String text = roundTripText(Codec.TSVECTOR, "tsvector", value);
        assertNotNull(text, "tsvector round-trip returned null for: " + value);
    }

}
