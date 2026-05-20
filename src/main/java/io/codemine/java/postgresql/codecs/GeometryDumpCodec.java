package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

/** Codec for PostGIS {@code geometry_dump} composites. */
final class GeometryDumpCodec implements Codec<GeometryDump> {
  private static final CompositeCodec<GeometryDump> DELEGATE =
      new CompositeCodec<>(
          "",
          "geometry_dump",
          (List<Integer> path) -> (Geometry geom) -> new GeometryDump(geom, path),
          new CompositeCodec.Field<>("path", GeometryDump::path, Codec.INT4.inDim()),
          new CompositeCodec.Field<>("geom", GeometryDump::geom, Codec.GEOMETRY));

  @Override
  public String name() {
    return DELEGATE.name();
  }

  @Override
  public void encodeInText(StringBuilder sb, GeometryDump value) {
    DELEGATE.encodeInText(sb, value);
  }

  @Override
  public Codec.ParsingResult<GeometryDump> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    return DELEGATE.decodeInText(input, offset);
  }

  @Override
  public void encodeInBinary(GeometryDump value, ByteArrayOutputStream out) {
    DELEGATE.encodeInBinary(value, out);
  }

  @Override
  public GeometryDump decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    return DELEGATE.decodeInBinary(buf, length);
  }

  @Override
  public GeometryDump random(Random r, int size) {
    return new GeometryDump(Codec.GEOMETRY.random(r, size), Codec.INT4.inDim().random(r, size));
  }
}
