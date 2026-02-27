package io.pgenie.postgresqlCodecs.codecs;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.postgresql.util.PGobject;

final class Macaddr8Codec implements Codec<String> {

    static final Macaddr8Codec instance = new Macaddr8Codec();

    private Macaddr8Codec() {
    }

    public String name() {
        return "macaddr8";
    }

    @Override
    public int oid() {
        return 774;
    }

    @Override
    public int arrayOid() {
        return 775;
    }

    @Override
    public void bind(PreparedStatement ps, int index, String value) throws SQLException {
        if (value != null) {
            PGobject obj = new PGobject();
            obj.setType("macaddr8");
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
        if (parts.length != 8) throw new RuntimeException("Invalid macaddr8: " + value);
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) b[i] = (byte) Integer.parseInt(parts[i], 16);
        return b;
    }

    @Override
    public String decodeBinary(ByteBuffer buf, int length) throws Codec.ParseException {
        if (length != 8) throw new Codec.ParseException("Binary macaddr8 must be 8 bytes, got " + length);
        byte[] b = new byte[8];
        buf.get(b);
        return String.format("%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x",
                b[0] & 0xff, b[1] & 0xff, b[2] & 0xff, b[3] & 0xff,
                b[4] & 0xff, b[5] & 0xff, b[6] & 0xff, b[7] & 0xff);
    }

}
