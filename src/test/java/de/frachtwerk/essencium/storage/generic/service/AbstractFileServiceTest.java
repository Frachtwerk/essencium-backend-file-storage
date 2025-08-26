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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.frachtwerk.essencium.storage.impl.identity.model.IdentityFile;
import de.frachtwerk.essencium.storage.impl.identity.model.IdentityStorageInfo;
import de.frachtwerk.essencium.storage.impl.identity.repository.IdentityFileRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AbstractFileServiceTest {
  @Mock private StorageServiceDispatcher<IdentityFile, Long, IdentityStorageInfo> dispatcher;
  @Mock private IdentityFileRepository repository;
  @Mock private MimeTypeHelper mimeTypeHelper;
  private AbstractFileService<IdentityFile, Long, IdentityStorageInfo> service;

  @BeforeEach
  void setUp() {
    service =
        new AbstractFileService<>(dispatcher, repository, mimeTypeHelper) {
          @Override
          protected IdentityFile getNewFile(
              List<IdentityStorageInfo> infos, String name, int length, String mimeType) {
            return new IdentityFile(infos, name, length, mimeType);
          }
        };
  }

  @Test
  @DisplayName("Store file with mime type given")
  void storeFile() throws IOException {
    byte[] fileContent = new byte[0];
    IdentityStorageInfo identityStorageInfo = mock(IdentityStorageInfo.class);
    when(identityStorageInfo.isAvailable()).thenReturn(true);
    when(dispatcher.saveFile("name", fileContent)).thenReturn(List.of(identityStorageInfo));
    when(repository.save(any(IdentityFile.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    try {
      IdentityFile file = service.storeFile("name", "mimeType", fileContent);
      assertNotNull(file);
      assertEquals("name", file.getName());
      assertEquals("mimeType", file.getMimeType());
      assertTrue(file.isAvailable());
    } catch (Exception e) {
      fail(e);
    }
    verify(dispatcher, times(1)).saveFile("name", fileContent);
    verify(repository, times(1)).save(any(IdentityFile.class));
    verifyNoMoreInteractions(dispatcher, repository, mimeTypeHelper);
  }

  @Test
  @DisplayName("Store file without mime type given")
  void storeFileWithoutMimeType() throws IOException {
    byte[] fileContent = new byte[0];
    IdentityStorageInfo identityStorageInfo = mock(IdentityStorageInfo.class);
    when(identityStorageInfo.isAvailable()).thenReturn(true);
    when(dispatcher.saveFile("name", fileContent)).thenReturn(List.of(identityStorageInfo));
    when(repository.save(any(IdentityFile.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    when(mimeTypeHelper.getMimeType("name", fileContent)).thenReturn("mimeType");
    try {
      IdentityFile file = service.storeFile("name", null, fileContent);
      assertNotNull(file);
      assertEquals("name", file.getName());
      assertEquals("mimeType", file.getMimeType());
      assertTrue(file.isAvailable());
    } catch (Exception e) {
      fail(e);
    }
    verify(dispatcher, times(1)).saveFile("name", fileContent);
    verify(repository, times(1)).save(any(IdentityFile.class));
    verify(mimeTypeHelper, times(1)).getMimeType("name", fileContent);
    verifyNoMoreInteractions(dispatcher, repository, mimeTypeHelper);
  }

  @Test
  @DisplayName("Load available file")
  void loadFile() {
    IdentityFile file = new IdentityFile();
    IdentityStorageInfo identityStorageInfo = new IdentityStorageInfo();
    identityStorageInfo.setAvailable(true);
    file.setId(42L);
    file.setStorageInfos(List.of(identityStorageInfo));

    when(repository.findById(42L)).thenReturn(Optional.of(file));

    when(dispatcher.loadFile(identityStorageInfo))
        .thenAnswer(
            invocationOnMock -> {
              IdentityStorageInfo argument = invocationOnMock.getArgument(0);
              argument.setContent(mock(Resource.class));
              return argument;
            });

    try {
      Optional<IdentityFile> loadedFile = service.loadFile(42L);
      assertTrue(loadedFile.isPresent());
      assertEquals(42L, loadedFile.get().getId());
      assertTrue(loadedFile.get().isAvailable());
      assertNotNull(loadedFile.get().getStorageInfos());
      assertNotNull(loadedFile.get().getContent());
    } catch (Exception e) {
      fail(e);
    }
    verify(dispatcher, times(1)).loadFile(identityStorageInfo);
    verify(repository, times(1)).findById(42L);
    verifyNoMoreInteractions(dispatcher, repository, mimeTypeHelper);
  }

  @Test
  @DisplayName("Load file - file not found")
  void loadFileNotFound() {
    IdentityFile file = new IdentityFile();
    IdentityStorageInfo identityStorageInfo = new IdentityStorageInfo();
    identityStorageInfo.setId(43L);
    identityStorageInfo.setAvailable(true);
    identityStorageInfo.setFile(file);
    file.setId(42L);
    file.setStorageInfos(List.of(identityStorageInfo));

    when(repository.findById(42L)).thenReturn(Optional.of(file));

    when(dispatcher.loadFile(identityStorageInfo))
        .thenAnswer(
            invocationOnMock -> {
              IdentityStorageInfo argument = invocationOnMock.getArgument(0);
              argument.setContent(null);
              argument.setAvailable(false);
              return argument;
            });

    try {
      Optional<IdentityFile> loadedFile = service.loadFile(42L);
      assertTrue(loadedFile.isPresent());
      assertEquals(42L, loadedFile.get().getId());
      assertFalse(loadedFile.get().isAvailable());
      assertNotNull(loadedFile.get().getStorageInfos());
      assertNull(loadedFile.get().getContent());
    } catch (Exception e) {
      fail(e);
    }

    verify(dispatcher, times(1)).loadFile(identityStorageInfo);
    verify(repository, times(1)).findById(42L);
    verify(repository, times(1)).save(file);
    verifyNoMoreInteractions(dispatcher, repository, mimeTypeHelper);
  }

  @Test
  @DisplayName("Load unavailable file")
  void loadUnavailableFile() {
    IdentityFile file = new IdentityFile();
    IdentityStorageInfo identityStorageInfo = new IdentityStorageInfo();
    identityStorageInfo.setAvailable(false);
    file.setId(42L);
    file.setStorageInfos(List.of(identityStorageInfo));

    when(repository.findById(42L)).thenReturn(Optional.of(file));

    try {
      Optional<IdentityFile> loadedFile = service.loadFile(42L);
      assertTrue(loadedFile.isPresent());
      assertEquals(42L, loadedFile.get().getId());
      assertFalse(loadedFile.get().isAvailable());
      assertNotNull(loadedFile.get().getStorageInfos());
      assertNull(loadedFile.get().getContent());
    } catch (Exception e) {
      fail(e);
    }
    verify(repository, times(1)).findById(42L);
    verifyNoMoreInteractions(dispatcher, repository, mimeTypeHelper);
  }

  @Test
  void getAll() {
    IdentityFile file = new IdentityFile();
    IdentityStorageInfo identityStorageInfo = new IdentityStorageInfo();
    identityStorageInfo.setAvailable(true);
    file.setId(42L);
    file.setStorageInfos(List.of(identityStorageInfo));

    when(repository.findAll()).thenReturn(List.of(file));

    when(dispatcher.loadFile(identityStorageInfo))
        .thenAnswer(
            invocationOnMock -> {
              IdentityStorageInfo argument = invocationOnMock.getArgument(0);
              argument.setContent(mock(Resource.class));
              return argument;
            });

    try {
      List<IdentityFile> loadedFiles = service.getAll();
      assertNotNull(loadedFiles);
      assertEquals(1, loadedFiles.size());
      assertEquals(42L, loadedFiles.getFirst().getId());
      assertTrue(loadedFiles.getFirst().isAvailable());
      assertNotNull(loadedFiles.getFirst().getStorageInfos());
      assertNotNull(loadedFiles.getFirst().getContent());
    } catch (Exception e) {
      fail(e);
    }
    verify(dispatcher, times(1)).loadFile(identityStorageInfo);
    verify(repository, times(1)).findAll();
    verifyNoMoreInteractions(dispatcher, repository, mimeTypeHelper);
  }

  @Test
  void getAllFiltered() {
    IdentityFile file = new IdentityFile();
    IdentityStorageInfo identityStorageInfo = new IdentityStorageInfo();
    identityStorageInfo.setAvailable(true);
    file.setId(42L);
    file.setStorageInfos(List.of(identityStorageInfo));

    Specification<IdentityFile> specification = mock(Specification.class);

    when(repository.findAll(specification)).thenReturn(List.of(file));

    when(dispatcher.loadFile(identityStorageInfo))
        .thenAnswer(
            invocationOnMock -> {
              IdentityStorageInfo argument = invocationOnMock.getArgument(0);
              argument.setContent(mock(Resource.class));
              return argument;
            });

    try {
      List<IdentityFile> loadedFiles = service.getAllFiltered(specification);
      assertNotNull(loadedFiles);
      assertEquals(1, loadedFiles.size());
      assertEquals(42L, loadedFiles.getFirst().getId());
      assertTrue(loadedFiles.getFirst().isAvailable());
      assertNotNull(loadedFiles.getFirst().getStorageInfos());
      assertNotNull(loadedFiles.getFirst().getContent());
    } catch (Exception e) {
      fail(e);
    }
    verify(dispatcher, times(1)).loadFile(identityStorageInfo);
    verify(repository, times(1)).findAll(specification);
    verifyNoMoreInteractions(dispatcher, repository, mimeTypeHelper);
  }

  @Test
  void loadFromProviders() {
    // TODO: Implement
  }

  @Test
  void deleteFile() {
    // TODO: Implement
  }

  @Test
  void markAsUnavailable() {
    // TODO: Implement
  }
}
