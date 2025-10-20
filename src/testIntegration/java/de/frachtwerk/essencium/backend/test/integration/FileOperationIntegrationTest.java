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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.frachtwerk.essencium.backend.model.dto.LoginRequest;
import de.frachtwerk.essencium.backend.test.integration.util.TestUtil;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceFile;
import de.frachtwerk.essencium.storage.impl.sequence.provider.s3.S3SequenceStorageInfo;
import de.frachtwerk.essencium.storage.impl.sequence.repository.SequenceFileRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import java.time.Duration;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
    classes = IntegrationTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class FileOperationIntegrationTest {
  private static final String BUCKET_NAME = "bucket";

  @Container
  static final MinIOContainer MINIO =
      new MinIOContainer("minio/minio:latest").withUserName("minio").withPassword("minio123");

  @Container
  static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:17");

  @DynamicPropertySource
  static void configure(DynamicPropertyRegistry registry) {
    if (!POSTGRES_CONTAINER.isRunning()) {
      POSTGRES_CONTAINER.withMinimumRunningDuration(Duration.ofSeconds(5)).start();
    }
    registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);

    if (!MINIO.isRunning()) {
      MINIO.start();
    }
    registry.add("file.enabled", () -> "true");
    registry.add("file.storage.usedProviders", () -> "s3");
    registry.add("file.storage.s3.active", () -> "true");
    registry.add("file.storage.s3.endpointUrl", MINIO::getS3URL);
    registry.add("file.storage.s3.accessKey", MINIO::getUserName);
    registry.add("file.storage.s3.secretKey", MINIO::getPassword);
    registry.add("file.storage.s3.bucketName", () -> BUCKET_NAME);
    registry.add("file.storage.s3.region", () -> "local");
  }

  private final WebApplicationContext webApplicationContext;
  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final SequenceFileRepository sequenceFileRepository;

  private final TestUtil testUtil;

  private MinioClient minioClient;
  private String accessToken;

  @Autowired
  public FileOperationIntegrationTest(
      WebApplicationContext webApplicationContext,
      MockMvc mockMvc,
      ObjectMapper objectMapper,
      SequenceFileRepository sequenceFileRepository,
      TestUtil testUtil) {
    this.webApplicationContext = webApplicationContext;
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
    this.sequenceFileRepository = sequenceFileRepository;
    this.testUtil = testUtil;
  }

  @BeforeEach
  void setUp() throws Exception {
    if (accessToken == null) {
      LoginRequest loginRequest = new LoginRequest("devnull@frachtwerk.de", "adminAdminAdmin");
      String loginRequestJson = objectMapper.writeValueAsString(loginRequest);

      ResultActions result =
          mockMvc
              .perform(
                  post("/auth/token")
                      .header("user-agent", "JUnit")
                      .content(loginRequestJson)
                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

      String resultString = result.andReturn().getResponse().getContentAsString();
      JsonNode responseJson = objectMapper.readTree(resultString);
      accessToken = responseJson.get("token").asText();
    }

    if (minioClient == null) {
      minioClient =
          MinioClient.builder()
              .endpoint(MINIO.getS3URL())
              .credentials(MINIO.getUserName(), MINIO.getPassword())
              .build();
    }

    boolean exists =
        minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
    if (!exists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
    }
  }

  @Test
  void testUploadFile() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "test.txt", "text/plain", "This is a test file.".getBytes());

    MvcResult result =
        mockMvc
            .perform(
                multipart("/v1/files")
                    .file(file)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .param("name", "test.txt"))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("test.txt"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(20))
            .andExpect(MockMvcResultMatchers.jsonPath("$.mimeType").value("text/plain"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.available").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.storageInfos").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.storageInfos", hasSize(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.storageInfos[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.storageInfos[0].available").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.storageInfos[0].s3ObjectKey").isNotEmpty())
            .andReturn();

    String responseJson = result.getResponse().getContentAsString();
    String fileKey = objectMapper.readTree(responseJson).at("/storageInfos/0/s3ObjectKey").asText();

    StatObjectResponse statObjectResponse =
        minioClient.statObject(
            StatObjectArgs.builder().bucket(BUCKET_NAME).object(fileKey).build());
    // Verify the file exists in MinIO
    assertNotNull(statObjectResponse);
    assertEquals("application/octet-stream", statObjectResponse.contentType());
    assertEquals(20, statObjectResponse.size());
  }

  @Test
  void testDownloadFile() throws Exception {
    SequenceFile testFile = testUtil.createTestFile(BUCKET_NAME, minioClient);

    mockMvc
        .perform(
            get("/v1/files/{id}", testFile.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename='" + testFile.getName() + "'"))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain"))
        .andExpect(content().bytes("This is a test file for download.".getBytes()));
  }

  @Test
  void testDeleteFile() throws Exception {
    SequenceFile testFile = testUtil.createTestFile(BUCKET_NAME, minioClient);

    mockMvc
        .perform(
            delete("/v1/files/{id}", testFile.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk());

    // Verify the file is deleted from the database
    boolean existsInDb =
        sequenceFileRepository.findById(Objects.requireNonNull(testFile.getId())).isPresent();
    assertFalse(existsInDb);

    // Verify the file is deleted from MinIO
    S3SequenceStorageInfo first = (S3SequenceStorageInfo) testFile.getStorageInfos().getFirst();
    String fileKey = first.getS3ObjectKey();
    Exception exception =
        assertThrows(
            ErrorResponseException.class,
            () ->
                minioClient.statObject(
                    StatObjectArgs.builder().bucket(BUCKET_NAME).object(fileKey).build()));
    assertEquals("Object does not exist", exception.getMessage());
  }
}
