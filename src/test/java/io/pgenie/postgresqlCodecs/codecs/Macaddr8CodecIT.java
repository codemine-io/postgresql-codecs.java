package io.pgenie.postgresqlCodecs.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class Macaddr8CodecIT extends CodecITBase {

    @Test
    void macaddr8RoundTrip() throws Exception {
        assertEquals("08:00:2b:01:02:03:04:05",
                roundTrip(Codec.MACADDR8, "08:00:2b:01:02:03:04:05"));
    }

    @Test
    void macaddr8Null() throws Exception {
        assertNull(roundTrip(Codec.MACADDR8, null));
    }


    @Test
    void macaddr8Oid() throws Exception {
        assertOid(Codec.MACADDR8);
    }

    @Test
    void macaddr8Binary() throws Exception {
        assertBinaryRoundTrip(Codec.MACADDR8, "macaddr8", "08:00:2b:ff:fe:01:02:03");
    }

    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#macaddr8s")
    void macaddr8PropertyRoundTrip(String value) throws Exception {
        assertEquals(value, roundTrip(Codec.MACADDR8, value));
    }

    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#macaddr8s")
    void macaddr8PropertyBinaryRoundTrip(String value) throws Exception {
        assertBinaryRoundTrip(Codec.MACADDR8, "macaddr8", value);
    }

}
