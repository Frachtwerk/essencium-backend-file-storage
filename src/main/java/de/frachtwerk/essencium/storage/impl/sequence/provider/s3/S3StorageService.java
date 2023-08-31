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

package de.frachtwerk.essencium.storage.impl.sequence.provider.s3;

import de.frachtwerk.essencium.storage.generic.provider.s3.AbstractS3StorageInfo;
import de.frachtwerk.essencium.storage.generic.provider.s3.AbstractS3StorageService;
import de.frachtwerk.essencium.storage.generic.provider.s3.S3StorageConfiguration;
import de.frachtwerk.essencium.storage.generic.service.MimeTypeHelper;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceFile;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceStorageInfo;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

@Service
public class S3StorageService
    extends AbstractS3StorageService<SequenceFile, Long, SequenceStorageInfo> {

  public S3StorageService(@NotNull S3StorageConfiguration config, MimeTypeHelper mimeTypeHelper) {
    super(config, mimeTypeHelper);
  }

  @Override
  protected AbstractS3StorageInfo<SequenceFile, Long, SequenceStorageInfo>
      getNewAbstractS3StorageInfo(SequenceFile file, String s3ObjectKey) {
    return S3SequenceStorageInfo.builder().file(file).s3ObjectKey(s3ObjectKey).build();
  }
}
