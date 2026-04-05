package io.codemine.java.postgresql.codecs;

import java.sql.DriverManager;

/**
 * Integration tests for {@link CitextCodec} against a real PostgreSQL instance.
 *
 * <p>The {@code citext} extension is installed once before the first test runs, using the shared
 * container from {@link CodecITBase}.
 */
public class CitextCodecIT extends CodecITBase<String> {

  static {
    try (var conn =
        DriverManager.getConnection(
            container.getJdbcUrl(), container.getUsername(), container.getPassword())) {
      conn.createStatement().execute("CREATE EXTENSION IF NOT EXISTS citext");
    } catch (Exception e) {
      throw new RuntimeException("Failed to install citext extension", e);
    }
  }

  public CitextCodecIT() {
    super(Codec.CITEXT, String.class);
  }
}
