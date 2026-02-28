package io.pgenie.postgresqlCodecs.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class MacaddrCodecIT extends CodecITBase {

    @Test
    void macaddrRoundTrip() throws Exception {
        assertEquals("08:00:2b:01:02:03", roundTrip(Codec.MACADDR, "08:00:2b:01:02:03"));
    }

    @Test
    void macaddrNull() throws Exception {
        assertNull(roundTrip(Codec.MACADDR, null));
    }


    @Test
    void macaddrOid() throws Exception {
        assertOid(Codec.MACADDR);
    }

    @Test
    void macaddrBinary() throws Exception {
        assertBinaryRoundTrip(Codec.MACADDR, "macaddr", "08:00:2b:01:02:03");
        assertBinaryRoundTrip(Codec.MACADDR, "macaddr", "ff:ff:ff:ff:ff:ff");
    }

    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#macaddrs")
    void macaddrPropertyRoundTrip(String value) throws Exception {
        assertEquals(value, roundTrip(Codec.MACADDR, value));
    }

    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#macaddrs")
    void macaddrPropertyBinaryRoundTrip(String value) throws Exception {
        assertBinaryRoundTrip(Codec.MACADDR, "macaddr", value);
    }

}
