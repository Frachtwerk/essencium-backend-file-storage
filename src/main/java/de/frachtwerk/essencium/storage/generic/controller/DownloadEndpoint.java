/*
 * Copyright (C) 2023 Frachtwerk GmbH, Leopoldstraße 7C, 76133 Karlsruhe.
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

import de.frachtwerk.essencium.backend.model.exception.ResourceNotFoundException;
import de.frachtwerk.essencium.storage.generic.model.AbstractFile;
import de.frachtwerk.essencium.storage.generic.model.AbstractStorageInfo;
import de.frachtwerk.essencium.storage.generic.service.FileService;
import de.frachtwerk.essencium.storage.generic.service.UniqueNameCreator;
import jakarta.validation.constraints.NotNull;
import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DownloadEndpoint<
    F extends AbstractFile<F, ID, S>,
    ID extends Serializable,
    S extends AbstractStorageInfo<F, ID, S>> {
  private static final Logger LOG = LoggerFactory.getLogger(DownloadEndpoint.class);

  private final FileService<F, ID, S> service;
  private final UniqueNameCreator uniqueNameCreator;

  @NotNull
  public ResponseEntity<Resource> prepareResponse(ID id) {
    F file =
        service.loadFile(id).orElseThrow(() -> new ResourceNotFoundException(String.valueOf(id)));
    return prepareResponse(file);
  }

  @NotNull
  public ResponseEntity<Resource> prepareResponse(F file) {
    Resource resource = getResource(file);
    // Fallback to the default content type if type could not be determined
    String mimeType = Optional.ofNullable(file.getMimeType()).orElse("application/octet-stream");

    return buildResponse(resource, file.getName(), MediaType.parseMediaType(mimeType));
  }

  public ResponseEntity<Resource> prepareZipResponse(
      String responseFileName, Collection<F> abstractFiles) throws IOException {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream =
            new BufferedOutputStream(byteArrayOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream)) {
      Set<String> names = new HashSet<>();
      for (F f : abstractFiles) {
        final InputStream inputStream = getResource(f).getInputStream();
        String name = uniqueNameCreator.getUniqueName(f.getName(), names::contains);
        names.add(name);
        zipOutputStream.putNextEntry(new ZipEntry(name));
        IOUtils.copy(inputStream, zipOutputStream);
        zipOutputStream.closeEntry();
      }
      zipOutputStream.finish();
      zipOutputStream.flush();

      try (final ByteArrayInputStream inputStream =
          new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
        Resource r = new InputStreamResource(inputStream);
        return buildResponse(r, responseFileName, MediaType.parseMediaType("application/zip"));
      }
    }
  }

  private ResponseEntity<Resource> buildResponse(
      Resource resource, String name, MediaType contentType) {
    return ResponseEntity.ok()
        .contentType(contentType)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name)
        .body(resource);
  }

  private Resource getResource(F file) {
    if (file.isAvailable()) {
      for (var storage : file.getStorageInfos()) {
        if (storage.isAvailable()) {
          Resource resource = storage.getContent();
          if (resource != null) {
            if (resource.exists()) {
              return resource;
            }
          } else {
            LOG.warn("File is marked available but resource is null");
            service.markAsUnavailable(storage);
          }
        } else {
          LOG.warn("File {} in {} is not available.", file.getId(), storage);
          service.markAsUnavailable(storage);
        }
      }
    }
    throw new FileNotAvailableException("File not found");
  }
}
