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

package de.frachtwerk.essencium.storage.impl.sequence.service;

import de.frachtwerk.essencium.storage.generic.service.AbstractFileService;
import de.frachtwerk.essencium.storage.generic.service.MimeTypeHelper;
import de.frachtwerk.essencium.storage.generic.service.StorageServiceDispatcher;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceFile;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceStorageInfo;
import de.frachtwerk.essencium.storage.impl.sequence.repository.SequenceFileRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DefaultSequenceFileService
    extends AbstractFileService<SequenceFile, Long, SequenceStorageInfo> {
  public DefaultSequenceFileService(
      StorageServiceDispatcher<SequenceFile, Long, SequenceStorageInfo> dispatcher,
      SequenceFileRepository repository,
      MimeTypeHelper mimeTypeHelper) {
    super(dispatcher, repository, mimeTypeHelper);
  }

  @Override
  protected SequenceFile getNewFile(
      List<SequenceStorageInfo> infos, String name, int length, String mimeType) {
    return new SequenceFile(infos, name, length, mimeType);
  }
}
