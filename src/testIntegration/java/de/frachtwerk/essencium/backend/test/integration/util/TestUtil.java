package de.frachtwerk.essencium.backend.test.integration.util;

import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceFile;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceStorageInfo;
import de.frachtwerk.essencium.storage.impl.sequence.provider.s3.S3SequenceStorageInfo;
import de.frachtwerk.essencium.storage.impl.sequence.repository.SequenceFileRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestUtil {

  private final SequenceFileRepository sequenceFileRepository;

  @Autowired
  public TestUtil(SequenceFileRepository sequenceFileRepository) {
    this.sequenceFileRepository = sequenceFileRepository;
  }

  @Transactional
  public SequenceFile save(SequenceFile sequenceFile) {
    return sequenceFileRepository.save(sequenceFile);
  }

  @Transactional
  public SequenceFile createTestFile(String bucketName, MinioClient minioClient) {
    UUID fileKey = UUID.randomUUID();
    MockMultipartFile file =
        new MockMultipartFile(
            "fileName",
            "originalFileName",
            "text/plain",
            "This is a test file for download.".getBytes());
    try (InputStream inputStream = file.getInputStream()) {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(fileKey.toString()).stream(
                  inputStream, file.getSize(), -1)
              .contentType("text/plain")
              .build());
    } catch (Exception e) {
      throw new RuntimeException("MinIO upload failed", e);
    }
    SequenceFile storedFile = new SequenceFile();
    storedFile.setName("originalFileName");
    storedFile.setMimeType("text/plain");
    storedFile.setSize(file.getSize());
    storedFile = sequenceFileRepository.save(storedFile);
    SequenceStorageInfo storageInfo =
        S3SequenceStorageInfo.builder()
            .file(storedFile)
            .s3ObjectKey(fileKey.toString())
            .available(true)
            .build();
    storedFile.setStorageInfos(new ArrayList<>(List.of(storageInfo)));
    return sequenceFileRepository.save(storedFile);
  }
}
