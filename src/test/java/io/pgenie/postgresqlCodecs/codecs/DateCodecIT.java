package io.pgenie.postgresqlCodecs.codecs;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DateCodecIT extends CodecITBase {

    @Test
    void dateRoundTrip() throws Exception {
        assertEquals(LocalDate.of(2024, 6, 15),
                roundTrip(Codec.DATE, LocalDate.of(2024, 6, 15)));
    }

    @Test
    void dateNull() throws Exception {
        assertNull(roundTrip(Codec.DATE, null));
    }


    @Test
    void dateOid() throws Exception {
        assertOid(Codec.DATE);
    }

    @Test
    void dateBinary() throws Exception {
        assertBinaryRoundTrip(Codec.DATE, "date", LocalDate.of(2000, 1, 1));
        assertBinaryRoundTrip(Codec.DATE, "date", LocalDate.of(1970, 1, 1));
        assertBinaryRoundTrip(Codec.DATE, "date", LocalDate.of(2024, 12, 31));
        assertBinaryRoundTrip(Codec.DATE, "date", LocalDate.of(1900, 6, 15));
    }

    /**
     * Property: arbitrary dates spanning PostgreSQL's full range (4713 BC to 5874897 AD)
     * round-trip through both the text and binary codecs.
     */
    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#dates")
    void datePropertyRoundTrip(LocalDate value) throws Exception {
        assertEquals(value, roundTrip(Codec.DATE, value));
    }

    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#dates")
    void datePropertyBinaryRoundTrip(LocalDate value) throws Exception {
        assertBinaryRoundTrip(Codec.DATE, "date", value);
    }

}
