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
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Service able of permanently writing files and read them again later.
 *
 * @param <F> File to be handled
 * @param <ID> Data type of the file id
 * @param <S> Storage info of the file
 */
@Service
public interface FileService<
    F extends AbstractFile<F, ID, S>,
    ID extends Serializable,
    S extends AbstractStorageInfo<F, ID, S>> {
  /**
   * Stores a file in the configured storages.
   *
   * @param name Name of the file
   * @param mimeType Mime type of the file
   * @param fileContent Content of the file as byte array
   * @return Storage info of the file
   * @throws IOException If the file could not be stored
   */
  F storeFile(String name, String mimeType, final byte[] fileContent) throws IOException;

  Optional<F> loadFile(final ID fileId);

  @NotNull
  List<F> getAll();

  @NotNull
  List<F> getAllFiltered(Specification<F> specification);

  @NotNull
  F loadFromProviders(@NotNull F f);

  boolean deleteFile(ID fileId);

  void markAsUnavailable(S info);
}
