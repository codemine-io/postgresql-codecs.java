package io.pgenie.postgresqlCodecs.codecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CidrCodecIT extends CodecITBase {

    @Test
    void cidrRoundTrip() throws Exception {
        assertEquals("192.168.1.0/24", roundTrip(Codec.CIDR, "192.168.1.0/24"));
    }

    @Test
    void cidrNull() throws Exception {
        assertNull(roundTrip(Codec.CIDR, null));
    }


    @Test
    void cidrOid() throws Exception {
        assertOid(Codec.CIDR);
    }

    @ParameterizedTest
    @MethodSource("io.pgenie.postgresqlCodecs.codecs.Generators#cidrIpv4s")
    void cidrPropertyRoundTrip(String value) throws Exception {
        assertEquals(value, roundTrip(Codec.CIDR, value));
    }

}
