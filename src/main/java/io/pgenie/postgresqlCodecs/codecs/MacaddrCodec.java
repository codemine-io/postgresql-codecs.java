package io.pgenie.postgresqlCodecs.codecs;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.postgresql.util.PGobject;

final class MacaddrCodec implements Codec<String> {

    static final MacaddrCodec instance = new MacaddrCodec();

    private MacaddrCodec() {
    }

    public String name() {
        return "macaddr";
    }

    @Override
    public int oid() {
        return 829;
    }

    @Override
    public int arrayOid() {
        return 1040;
    }

    @Override
    public void bind(PreparedStatement ps, int index, String value) throws SQLException {
        if (value != null) {
            PGobject obj = new PGobject();
            obj.setType("macaddr");
            obj.setValue(value);
            ps.setObject(index, obj);
        } else {
            ps.setNull(index, Types.OTHER);
        }
    }

    public void write(StringBuilder sb, String value) {
        sb.append(value);
    }

    @Override
    public Codec.ParsingResult<String> parse(CharSequence input, int offset) throws Codec.ParseException {
        return new Codec.ParsingResult<>(input.subSequence(offset, input.length()).toString(), input.length());
    }

    @Override
    public byte[] encode(String value) {
        String[] parts = value.split(":");
        if (parts.length != 6) throw new RuntimeException("Invalid macaddr: " + value);
        byte[] b = new byte[6];
        for (int i = 0; i < 6; i++) b[i] = (byte) Integer.parseInt(parts[i], 16);
        return b;
    }

    @Override
    public String decodeBinary(ByteBuffer buf, int length) throws Codec.ParseException {
        if (length != 6) throw new Codec.ParseException("Binary macaddr must be 6 bytes, got " + length);
        byte[] b = new byte[6];
        buf.get(b);
        return String.format("%02x:%02x:%02x:%02x:%02x:%02x",
                b[0] & 0xff, b[1] & 0xff, b[2] & 0xff, b[3] & 0xff, b[4] & 0xff, b[5] & 0xff);
    }

}
