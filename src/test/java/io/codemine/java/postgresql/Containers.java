package io.codemine.java.postgresql;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class Containers {
  private static final DockerImageName IMAGE =
      DockerImageName.parse("postgis/postgis:18-3.6").asCompatibleSubstituteFor("postgres");

  private Containers() {}

  public static PostgreSQLContainer<?> newPostgresContainer() {
    return new PostgreSQLContainer<>(IMAGE)
        .withInitScript("init-container.sql")
        .withCreateContainerCmdModifier(cmd -> cmd.withPlatform("linux/amd64"));
  }
}
