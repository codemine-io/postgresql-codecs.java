package io.codemine.java.postgresql.codecs;

import java.sql.DriverManager;

/**
 * Integration tests for {@link HstoreCodec} against a real PostgreSQL instance.
 *
 * <p>The {@code hstore} extension is installed once before the first test runs, using the shared
 * container from {@link CodecITBase}.
 */
public class HstoreCodecIT extends CodecITBase<Hstore> {

  static {
    try (var conn =
        DriverManager.getConnection(
            container.getJdbcUrl(), container.getUsername(), container.getPassword())) {
      conn.createStatement().execute("CREATE EXTENSION IF NOT EXISTS hstore");
    } catch (Exception e) {
      throw new RuntimeException("Failed to install hstore extension", e);
    }
  }

  public HstoreCodecIT() {
    super(Codec.HSTORE, Hstore.class);
  }
}
