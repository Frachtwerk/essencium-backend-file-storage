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

package de.frachtwerk.essencium.storage.generic.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.frachtwerk.essencium.storage.generic.configuration.FileStorageConfiguration;
import de.frachtwerk.essencium.storage.generic.model.Providers;
import de.frachtwerk.essencium.storage.generic.provider.local.AbstractLocalStorageService;
import de.frachtwerk.essencium.storage.generic.provider.s3.AbstractS3StorageService;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceFile;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceStorageInfo;
import de.frachtwerk.essencium.storage.impl.sequence.provider.local.LocalSequenceStorageInfo;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class StorageServiceDispatcherTest {

  @Test
  @SuppressWarnings("unchecked")
  void failingProviderRemovesTheCopiesAlreadyStored() throws Exception {
    AbstractLocalStorageService<SequenceFile, Long, SequenceStorageInfo> local =
        mock(AbstractLocalStorageService.class);
    AbstractS3StorageService<SequenceFile, Long, SequenceStorageInfo> s3 =
        mock(AbstractS3StorageService.class);
    FileStorageConfiguration config = new FileStorageConfiguration();
    config.setUsedProviders(new LinkedHashSet<>(List.of(Providers.LOCAL, Providers.S3)));
    StorageServiceDispatcher<SequenceFile, Long, SequenceStorageInfo> dispatcher =
        new StorageServiceDispatcher<>(local, s3, config);

    LocalSequenceStorageInfo localCopy = new LocalSequenceStorageInfo(null, "/storage/copy");
    when(local.saveFile(any(), any())).thenReturn(localCopy);
    when(s3.saveFile(any(), any())).thenThrow(new IllegalStateException("S3 not reachable"));

    assertThrows(IllegalStateException.class, () -> dispatcher.saveFile("name.txt", new byte[1]));
    verify(local).deleteFile(localCopy);
  }
}
