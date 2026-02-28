package io.pgenie.postgresqlCodecs.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class BitCodecIT extends CodecITBase {

    @Test
    void bitRoundTrip() throws Exception {
        assertEquals("1", roundTrip(Codec.BIT, "1"));
    }

    @Test
    void bitNull() throws Exception {
        assertNull(roundTrip(Codec.BIT, null));
    }


    @Test
    void bitOid() throws Exception {
        assertOid(Codec.BIT);
    }

    @Test
    void bitBinary() throws Exception {
        // pgType must match exact bit length
        String value = "101011";
        byte[] pgBytes = pgBinaryBytes(Codec.BIT, "bit(6)", value);
        assertEquals(hex(pgBytes), hex(Codec.BIT.encode(value)));
        assertEquals(value, Codec.BIT.decodeBinary(wrap(pgBytes), pgBytes.length));
    }

    /**
     * Property: arbitrary bit strings round-trip through text and binary codecs.
     *
     * <p>Uses the dynamic {@code bit(n)} type to match the generated string length.
     */
    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#bits")
    void bitPropertyTextRoundTrip(String value) throws Exception {
        String text = roundTripText(Codec.BIT, "bit(" + value.length() + ")", value);
        assertNotNull(text);
        assertEquals(value, Codec.BIT.parse(text, 0).value);
    }

    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#bits")
    void bitPropertyBinaryRoundTrip(String value) throws Exception {
        byte[] pgBytes = pgBinaryBytes(Codec.BIT, "bit(" + value.length() + ")", value);
        assertEquals(hex(pgBytes), hex(Codec.BIT.encode(value)));
        assertEquals(value, Codec.BIT.decodeBinary(wrap(pgBytes), pgBytes.length));
    }

}
