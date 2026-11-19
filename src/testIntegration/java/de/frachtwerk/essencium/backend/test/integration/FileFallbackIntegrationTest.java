/*
 * Copyright (C) 2025 Frachtwerk GmbH, Leopoldstraße 7C, 76133 Karlsruhe.
 *
 * This file is part of essencium-backend.
 *
 * essencium-backend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * essencium-backend is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with essencium-backend. If not, see <http://www.gnu.org/licenses/>.
 */

package de.frachtwerk.essencium.backend.test.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.frachtwerk.essencium.backend.model.dto.LoginRequest;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Stores files with the local and the S3 provider and checks the fallback between them. */
@Testcontainers
@SpringBootTest(
    classes = IntegrationTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class FileFallbackIntegrationTest {
  private static final String BUCKET_NAME = "fallback";
  private static final int S3_PORT = 9000;
  private static final String S3_USER = "s3user";
  private static final String S3_PASSWORD = "s3password";
  private static final String S3_REGION = "eu-central-1";

  @Container
  static final GenericContainer<?> S3_CONTAINER =
      new GenericContainer<>("rustfs/rustfs:latest")
          .withEnv(
              Map.of(
                  "RUSTFS_ACCESS_KEY", S3_USER,
                  "RUSTFS_SECRET_KEY", S3_PASSWORD,
                  "RUSTFS_ADDRESS", ":" + S3_PORT,
                  "RUSTFS_REGION", S3_REGION))
          .withExposedPorts(S3_PORT)
          .waitingFor(Wait.forHttp("/health/ready").forPort(S3_PORT).forStatusCode(200));

  @Container
  static final PostgreSQLContainer POSTGRES_CONTAINER = new PostgreSQLContainer("postgres:17");

  @DynamicPropertySource
  static void configure(DynamicPropertyRegistry registry) throws Exception {
    registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);

    Path localStorage = Files.createTempDirectory("file-fallback-integration-test");
    registry.add("file.enabled", () -> "true");
    registry.add("file.storage.usedProviders", () -> "local,s3");
    registry.add("file.storage.local.active", () -> "true");
    registry.add("file.storage.local.path", localStorage::toString);
    registry.add("file.storage.s3.active", () -> "true");
    registry.add("file.storage.s3.endpointUrl", FileFallbackIntegrationTest::s3Url);
    registry.add("file.storage.s3.accessKey", () -> S3_USER);
    registry.add("file.storage.s3.secretKey", () -> S3_PASSWORD);
    registry.add("file.storage.s3.bucketName", () -> BUCKET_NAME);
    registry.add("file.storage.s3.region", () -> S3_REGION);
  }

  private static String s3Url() {
    return "http://" + S3_CONTAINER.getHost() + ":" + S3_CONTAINER.getMappedPort(S3_PORT);
  }

  @BeforeAll
  static void createBucket() throws Exception {
    MinioClient.builder()
        .endpoint(s3Url())
        .credentials(S3_USER, S3_PASSWORD)
        .region(S3_REGION)
        .build()
        .makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void missingLocalCopyIsDownloadedFromS3OnEveryRequest() throws Exception {
    String accessToken = login();
    byte[] content = "stored with both providers".getBytes(StandardCharsets.UTF_8);
    JsonNode stored =
        objectMapper.readTree(
            mockMvc
                .perform(
                    multipart("/v1/files")
                        .file(new MockMultipartFile("file", "both.txt", "text/plain", content))
                        .param("name", "both.txt")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

    Path localCopy = null;
    for (JsonNode info : stored.get("storageInfos")) {
      if (info.hasNonNull("path")) {
        localCopy = Path.of(info.get("path").asString());
      }
    }
    assertTrue(localCopy != null && Files.deleteIfExists(localCopy));

    for (int request = 1; request <= 2; request++) {
      mockMvc
          .perform(
              get("/v1/files/" + stored.get("id").asLong())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
          .andExpect(status().isOk())
          .andExpect(content().bytes(content));
    }
  }

  private String login() throws Exception {
    String response =
        mockMvc
            .perform(
                post("/auth/token")
                    .header(HttpHeaders.USER_AGENT, "JUnit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            new LoginRequest("devnull@frachtwerk.de", "adminAdminAdmin"))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(response).get("token").asString();
  }
}
