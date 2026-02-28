package io.pgenie.postgresqlCodecs.codecs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class IntervalCodecIT extends CodecITBase {

    @Test
    void intervalRoundTrip() throws Exception {
        // PostgreSQL normalizes intervals; "1 year 2 mons 3 days" is canonical
        String text = roundTripText(Codec.INTERVAL, "interval", "1 year 2 mons 3 days");
        assertNotNull(text);
        assertTrue(text.contains("1 year"));
    }

    @Test
    void intervalNull() throws Exception {
        assertNull(roundTripText(Codec.INTERVAL, "interval", null));
    }

    /**
     * Property: generated interval literals are accepted by PostgreSQL.
     *
     * <p>PostgreSQL normalizes intervals (e.g. 12 months → 1 year), so only
     * non-null acceptance is asserted rather than exact string equality.
     */
    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#intervals")
    void intervalPropertyRoundTrip(String value) throws Exception {
        assertNotNull(roundTripText(Codec.INTERVAL, "interval", value),
                "interval round-trip returned null for: " + value);
    }

}
