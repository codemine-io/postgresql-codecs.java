package io.pgenie.postgresqlCodecs.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class VarbitCodecIT extends CodecITBase {

    @Test
    void varbitRoundTrip() throws Exception {
        assertEquals("1011010", roundTrip(Codec.VARBIT, "1011010"));
    }

    @Test
    void varbitNull() throws Exception {
        assertNull(roundTrip(Codec.VARBIT, null));
    }


    @Test
    void varbitOid() throws Exception {
        assertOid(Codec.VARBIT);
    }

    @Test
    void varbitBinary() throws Exception {
        String value = "10110";
        byte[] pgBytes = pgBinaryBytes(Codec.VARBIT, "varbit", value);
        assertEquals(hex(pgBytes), hex(Codec.VARBIT.encode(value)));
        assertEquals(value, Codec.VARBIT.decodeBinary(wrap(pgBytes), pgBytes.length));
    }

    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#varbits")
    void varbitPropertyRoundTrip(String value) throws Exception {
        assertEquals(value, roundTrip(Codec.VARBIT, value));
    }

    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#varbits")
    void varbitPropertyBinaryRoundTrip(String value) throws Exception {
        assertBinaryRoundTrip(Codec.VARBIT, "varbit", value);
    }

}
