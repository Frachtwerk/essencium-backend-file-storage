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

package de.frachtwerk.essencium.storage.generic.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import de.frachtwerk.essencium.storage.generic.service.FileService;
import de.frachtwerk.essencium.storage.generic.service.UniqueNameCreator;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceFile;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceStorageInfo;
import de.frachtwerk.essencium.storage.impl.sequence.provider.local.LocalSequenceStorageInfo;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class DownloadEndpointTest {
  private DownloadEndpoint<SequenceFile, Long, SequenceStorageInfo> endpoint;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    endpoint = new DownloadEndpoint<>(mock(FileService.class), new UniqueNameCreator());
  }

  @Test
  void invalidStoredMimeTypeIsServedAsOctetStream() {
    ResponseEntity<Resource> response = endpoint.prepareResponse(file("report.txt", "notamime"));

    assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "report.txt",
        "it's.txt",
        "say \"hi\".txt",
        "a;filename=evil.exe",
        "Prüfbericht äöü.txt",
        "Übersicht 📄.txt",
        "line\r\nbreak.txt"
      })
  void contentDispositionCarriesTheFullNameInAValidHeader(String name) {
    ResponseEntity<Resource> response = endpoint.prepareResponse(file(name, "text/plain"));

    String header = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
    assertTrue(header.chars().allMatch(c -> c >= 0x20 && c < 0x7f), header);
    ContentDisposition parsed = ContentDisposition.parse(header);
    assertTrue(parsed.isAttachment());
    assertEquals(name, parsed.getFilename());
  }

  @Test
  void zipEntriesContainNoPathSegments() throws Exception {
    List<SequenceFile> files =
        List.of(
            file("../evil.txt", "text/plain"),
            file("/abs.txt", "text/plain"),
            file("a/b.txt", "text/plain"),
            file("dir\\win.txt", "text/plain"),
            file("..", "text/plain"));

    ResponseEntity<Resource> response = endpoint.prepareZipResponse("files.zip", files);

    List<String> entries = new ArrayList<>();
    try (InputStream in = response.getBody().getInputStream();
        ZipInputStream zip = new ZipInputStream(in)) {
      for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
        entries.add(entry.getName());
      }
    }
    assertEquals(files.size(), entries.size());
    for (String entry : entries) {
      assertTrue(
          !entry.isBlank()
              && !entry.contains("/")
              && !entry.contains("\\")
              && !entry.equals("..")
              && !entry.equals("."),
          entry);
    }
  }

  private static SequenceFile file(String name, String mimeType) {
    LocalSequenceStorageInfo info =
        LocalSequenceStorageInfo.builder().path("/storage/" + name).build();
    info.setContent(new ByteArrayResource("content".getBytes(StandardCharsets.UTF_8)));
    SequenceFile file = new SequenceFile(new ArrayList<>(List.of(info)), name, 7, mimeType);
    info.setFile(file);
    return file;
  }
}
