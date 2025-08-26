package de.frachtwerk.essencium.backend.test.integration.controller;

import de.frachtwerk.essencium.storage.generic.controller.DownloadEndpoint;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceFile;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceStorageInfo;
import de.frachtwerk.essencium.storage.impl.sequence.service.DefaultSequenceFileService;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/files")
public class TestDownloadController {
  private final DefaultSequenceFileService sequenceFileService;
  private final DownloadEndpoint<SequenceFile, Long, ? extends SequenceStorageInfo>
      downloadEndpoint;

  @Autowired
  public TestDownloadController(
      DefaultSequenceFileService sequenceFileService,
      DownloadEndpoint<SequenceFile, Long, ? extends SequenceStorageInfo> downloadEndpoint) {
    this.sequenceFileService = sequenceFileService;
    this.downloadEndpoint = downloadEndpoint;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public @NotNull SequenceFile createTestFile(
      @RequestParam(required = true) String name,
      @RequestParam(required = false) String mimeType,
      @RequestParam final MultipartFile file)
      throws Exception {
    String type = Optional.ofNullable(file.getContentType()).orElse(mimeType);
    return sequenceFileService.storeFile(name, type, file.getBytes());
  }

  @GetMapping("/{id}")
  public @NotNull @NotNull ResponseEntity<Resource> getTestFile(@PathVariable Long id) {
    SequenceFile sequenceFile = sequenceFileService.loadFile(id).orElseThrow();
    return downloadEndpoint.prepareResponse(sequenceFile);
  }

  @DeleteMapping("/{id}")
  public void deleteTestFile(@PathVariable Long id) {
    sequenceFileService.deleteFile(id);
  }
}
