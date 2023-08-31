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

package de.frachtwerk.essencium.storage.impl.identity.service;

import de.frachtwerk.essencium.storage.generic.service.AbstractFileService;
import de.frachtwerk.essencium.storage.generic.service.MimeTypeHelper;
import de.frachtwerk.essencium.storage.generic.service.StorageServiceDispatcher;
import de.frachtwerk.essencium.storage.impl.identity.model.IdentityFile;
import de.frachtwerk.essencium.storage.impl.identity.model.IdentityStorageInfo;
import de.frachtwerk.essencium.storage.impl.identity.repository.IdentityFileRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DefaultIdentityFileService
    extends AbstractFileService<IdentityFile, Long, IdentityStorageInfo> {
  public DefaultIdentityFileService(
      StorageServiceDispatcher<IdentityFile, Long, IdentityStorageInfo> dispatcher,
      IdentityFileRepository repository,
      MimeTypeHelper mimeTypeHelper) {
    super(dispatcher, repository, mimeTypeHelper);
  }

  @Override
  protected IdentityFile getNewFile(
      List<IdentityStorageInfo> infos, String name, int length, String mimeType) {
    return new IdentityFile(infos, name, length, mimeType);
  }
}
