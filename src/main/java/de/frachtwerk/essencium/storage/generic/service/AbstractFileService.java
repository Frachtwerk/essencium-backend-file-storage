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

package de.frachtwerk.essencium.storage.generic.service;

import de.frachtwerk.essencium.storage.generic.model.AbstractFile;
import de.frachtwerk.essencium.storage.generic.model.AbstractStorageInfo;
import de.frachtwerk.essencium.storage.generic.repository.AbstractFileRepository;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public abstract class AbstractFileService<
        F extends AbstractFile<F, ID, S>,
        ID extends Serializable,
        S extends AbstractStorageInfo<F, ID, S>>
    implements FileService<F, ID, S> {
  private final Logger LOG = LoggerFactory.getLogger(AbstractFileService.class);

  private final StorageServiceDispatcher<F, ID, S> dispatcher;
  private final AbstractFileRepository<F, ID, S> repository;
  private final MimeTypeHelper mimeTypeHelper;

  @Override
  public F storeFile(String name, String mimeType, byte[] fileContent) throws IOException {
    final List<S> infos = dispatcher.saveFile(name, fileContent);
    if (StringUtils.isBlank(mimeType)) {
      LOG.debug("MIME Type not set. Attempting to determine it...");
      mimeType = mimeTypeHelper.getMimeType(name, fileContent);
    }
    F file = getNewFile(infos, name, fileContent.length, mimeType);
    infos.forEach(i -> i.setFile(file));
    return repository.save(file);
  }

  protected abstract F getNewFile(List<S> infos, String name, int length, String mimeType);

  @Override
  public Optional<F> loadFile(ID id) {
    return repository.findById(id).map(this::loadFromProviders);
  }

  @NotNull
  @Override
  public List<F> getAll() {
    List<F> allEntities = repository.findAll();
    allEntities.forEach(this::loadFromProviders);
    return allEntities;
  }

  @NotNull
  @Override
  public List<F> getAllFiltered(Specification<F> specification) {
    final List<F> result = repository.findAll(specification);
    result.forEach(this::loadFromProviders);
    return result;
  }

  @NotNull
  @Override
  public F loadFromProviders(@NotNull F f) {
    for (S info : f.getStorageInfos()) {
      if (info.isAvailable()) {
        dispatcher.loadFile(info);
        if (info.getContent() != null) {
          break;
        } else if (!info.isAvailable()) {
          markAsUnavailable(info);
        }
      }
    }
    return f;
  }

  @Override
  public boolean deleteFile(ID id) {
    final Optional<F> file = repository.findById(id);
    try {
      return file.map(
              f ->
                  f.getStorageInfos().stream()
                      .map(dispatcher::deleteFile)
                      .reduce(true, (acc, v) -> acc && v))
          .orElse(false);
    } finally {
      if (file.isPresent()) {
        repository.delete(file.get());
        LOG.debug("Deleted file {}.", id);
      }
    }
  }

  @Override
  @CachePut(value = "files", key = "#result.id")
  public void markAsUnavailable(S info) {
    LOG.debug("Mark storage {} of file {} as unavailable.", info.getFile().getId(), info.getId());
    info.setAvailable(false);
    repository.save(info.getFile());
  }
}
