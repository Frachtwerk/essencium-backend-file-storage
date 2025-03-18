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

package de.frachtwerk.essencium.storage.impl.uuid.provider.local;

import de.frachtwerk.essencium.storage.generic.provider.local.AbstractLocalStorageInfo;
import de.frachtwerk.essencium.storage.generic.provider.local.AbstractLocalStorageService;
import de.frachtwerk.essencium.storage.generic.provider.local.LocalFileCreator;
import de.frachtwerk.essencium.storage.generic.provider.local.LocalStorageConfiguration;
import de.frachtwerk.essencium.storage.generic.service.UniqueNameCreator;
import de.frachtwerk.essencium.storage.impl.uuid.model.UUIDFile;
import de.frachtwerk.essencium.storage.impl.uuid.model.UUIDStorageInfo;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class LocalStorageService
    extends AbstractLocalStorageService<UUIDFile, UUID, UUIDStorageInfo> {

  public LocalStorageService(
      @NotNull LocalFileCreator fileCreator,
      @NotNull LocalStorageConfiguration config,
      @NotNull UniqueNameCreator uniqueNameCreator) {
    super(fileCreator, config, uniqueNameCreator);
  }

  @Override
  protected AbstractLocalStorageInfo<UUIDFile, UUID, UUIDStorageInfo> getNewLocalStorageInfo(
      UUIDFile file, String path) {
    return LocalUUIDStorageInfo.builder().file(file).path(path).build();
  }
}
