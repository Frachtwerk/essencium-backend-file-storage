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

import de.frachtwerk.essencium.storage.generic.configuration.FileStorageConfiguration;
import de.frachtwerk.essencium.storage.generic.model.AbstractFile;
import de.frachtwerk.essencium.storage.generic.model.AbstractStorageInfo;
import de.frachtwerk.essencium.storage.generic.model.Providers;
import de.frachtwerk.essencium.storage.generic.model.StorageInfoVisitor;
import de.frachtwerk.essencium.storage.generic.provider.local.AbstractLocalStorageInfo;
import de.frachtwerk.essencium.storage.generic.provider.local.AbstractLocalStorageService;
import de.frachtwerk.essencium.storage.generic.provider.s3.AbstractS3StorageInfo;
import de.frachtwerk.essencium.storage.generic.provider.s3.AbstractS3StorageService;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StorageServiceDispatcher<
    F extends AbstractFile<F, ID, S>,
    ID extends Serializable,
    S extends AbstractStorageInfo<F, ID, S>> {
  @Nullable private final AbstractLocalStorageService<F, ID, S> local;
  @Nullable private final AbstractS3StorageService<F, ID, S> s3;
  private final FileStorageConfiguration config;

  public List<S> saveFile(String originalName, byte[] content) throws IOException {
    List<S> infos = new ArrayList<>();
    for (Providers provider : config.getUsedProviders()) {
      switch (provider) {
        case LOCAL -> infos.add(Objects.requireNonNull(local).saveFile(originalName, content));
        case S3 -> infos.add(Objects.requireNonNull(s3).saveFile(originalName, content));
        default -> throw new IllegalArgumentException("Unknown provider");
      }
    }
    return infos;
  }

  public boolean deleteFile(S info) {
    return info.accept(
        new StorageInfoVisitor<Boolean, F, ID, S>() {
          @Override
          public Boolean visit(AbstractLocalStorageInfo<F, ID, S> info) {
            return Objects.requireNonNull(local).deleteFile((S) info);
          }

          @Override
          public Boolean visit(AbstractS3StorageInfo<F, ID, S> info) {
            return Objects.requireNonNull(s3).deleteFile((S) info);
          }
        });
  }

  public S loadFile(S info) {
    return info.accept(
        new StorageInfoVisitor<S, F, ID, S>() {
          @Override
          public S visit(AbstractLocalStorageInfo<F, ID, S> info) {
            return Objects.requireNonNull(local).loadFile((S) info);
          }

          @Override
          public S visit(AbstractS3StorageInfo<F, ID, S> info) {
            return Objects.requireNonNull(s3).loadFile((S) info);
          }
        });
  }
}
