package io.codemine.java.postgresql.codecs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

/** Codec for PostGIS {@code geometry} values. */
final class GeometryCodec implements Codec<Geometry> {

  @Override
  public String name() {
    return "geometry";
  }

  @Override
  public void encodeInText(StringBuilder sb, Geometry value) {
    sb.append(GeometrySerde.encodeHex(GeometrySerde.encodeGeometry(value)));
  }

  @Override
  public Codec.ParsingResult<Geometry> decodeInText(CharSequence input, int offset)
      throws Codec.DecodingException {
    byte[] bytes = GeometrySerde.decodeHex(input.subSequence(offset, input.length()));
    return new Codec.ParsingResult<>(
        GeometrySerde.decodeGeometry(ByteBuffer.wrap(bytes), bytes.length), input.length());
  }

  @Override
  public void encodeInBinary(Geometry value, ByteArrayOutputStream out) {
    out.writeBytes(GeometrySerde.encodeGeometry(value));
  }

  @Override
  public Geometry decodeInBinary(ByteBuffer buf, int length) throws Codec.DecodingException {
    return GeometrySerde.decodeGeometry(buf, length);
  }

  @Override
  public Codec<List<Geometry>> inDim() {
    return new ArrayCodec<>(this, ':');
  }

  @Override
  public Geometry random(Random r, int size) {
    return GeometrySerde.randomGeometry(r, size);
  }
}
