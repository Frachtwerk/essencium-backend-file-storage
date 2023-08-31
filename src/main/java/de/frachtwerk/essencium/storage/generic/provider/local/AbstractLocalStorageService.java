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

package de.frachtwerk.essencium.storage.generic.provider.local;

import de.frachtwerk.essencium.storage.generic.model.AbstractFile;
import de.frachtwerk.essencium.storage.generic.model.AbstractStorageInfo;
import de.frachtwerk.essencium.storage.generic.model.Providers;
import de.frachtwerk.essencium.storage.generic.service.StorageService;
import de.frachtwerk.essencium.storage.generic.service.UniqueNameCreator;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

@RequiredArgsConstructor
public abstract class AbstractLocalStorageService<
        F extends AbstractFile<F, ID, S>,
        ID extends Serializable,
        S extends AbstractStorageInfo<F, ID, S>>
    implements StorageService<F, ID, S> {
  private final Logger LOG = LoggerFactory.getLogger(AbstractLocalStorageService.class);

  @NotNull private final LocalFileCreator fileCreator;
  @NotNull private final LocalStorageConfiguration config;
  @NotNull private final UniqueNameCreator uniqueNameCreator;

  @Override
  public final S saveFile(String originalName, byte[] content) throws IOException {
    String name;
    if (originalName != null && config.isKeepFileName()) {
      name = uniqueNameCreator.getUniqueName(originalName, this::exists);
    } else {
      do {
        name = UUID.randomUUID().toString();
      } while (new java.io.File(name).exists());
    }
    java.io.File f = fileCreator.createFile(name);
    Files.write(f.toPath(), content);
    final String path = f.getAbsolutePath();
    LOG.debug("Saved file at {}.", path);
    final S info = getNewLocalStorageInfo(null, path);
    info.setContent(new FileSystemResource(f));
    return info;
  }

  protected abstract <SI extends AbstractLocalStorageInfo<F, ID, S>> SI getNewLocalStorageInfo(
      F file, String path);

  private boolean exists(String name) {
    Path path = Path.of(config.resolvePath().toString(), name);
    return Files.exists(path);
  }

  @Override
  public final boolean deleteFile(S abstractInfo) {
    AbstractLocalStorageInfo<F, ID, S> info = (AbstractLocalStorageInfo<F, ID, S>) abstractInfo;
    return new java.io.File(info.getPath()).delete();
  }

  @Override
  public final S loadFile(S abstractInfo) {
    AbstractLocalStorageInfo<F, ID, S> info = (AbstractLocalStorageInfo<F, ID, S>) abstractInfo;
    var file = new java.io.File(info.getPath());
    if (!file.exists()) {
      LOG.warn(
          "File {} with name {} is not present on disk.",
          info.getFile().getId(),
          Path.of(info.getPath()).getFileName());
      info.setAvailable(false);
    } else {
      info.setContent(new FileSystemResource(file));
    }
    return (S) info;
  }

  @Override
  public Providers getType() {
    return Providers.LOCAL;
  }
}
