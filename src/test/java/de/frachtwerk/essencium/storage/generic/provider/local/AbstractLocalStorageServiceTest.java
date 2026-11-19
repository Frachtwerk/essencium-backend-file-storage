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

package de.frachtwerk.essencium.storage.generic.provider.local;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.frachtwerk.essencium.storage.generic.service.UniqueNameCreator;
import de.frachtwerk.essencium.storage.impl.sequence.provider.local.LocalSequenceStorageInfo;
import de.frachtwerk.essencium.storage.impl.sequence.provider.local.LocalStorageService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AbstractLocalStorageServiceTest {
  private static final byte[] CONTENT = "content".getBytes(StandardCharsets.UTF_8);

  @TempDir Path parentDirectory;
  private Path storageDirectory;
  private LocalStorageService service;

  @BeforeEach
  void setUp() throws Exception {
    storageDirectory = Files.createDirectory(parentDirectory.resolve("storage"));
    LocalStorageConfiguration config = new LocalStorageConfiguration();
    config.setActive(true);
    config.setPath(storageDirectory.toString());
    config.setKeepFileName(true);
    service =
        new LocalStorageService(new LocalFileCreator(config), config, new UniqueNameCreator());
  }

  @ParameterizedTest
  @ValueSource(strings = {"../escape.txt", "sub/dir/escape.txt", "/escape.txt"})
  void keptFileNameStaysInsideTheStorageDirectory(String name) throws Exception {
    LocalSequenceStorageInfo info = (LocalSequenceStorageInfo) service.saveFile(name, CONTENT);

    Path stored = Path.of(info.getPath());
    assertEquals(storageDirectory, stored.getParent());
    assertEquals("escape.txt", stored.getFileName().toString());
    assertFalse(Files.exists(parentDirectory.resolve("escape.txt")));
  }

  @Test
  void parallelUploadsWithTheSameNameAllSucceed() throws Exception {
    int uploads = 20;
    Callable<String> upload =
        () -> ((LocalSequenceStorageInfo) service.saveFile("same.txt", CONTENT)).getPath();
    List<Future<String>> results;
    try (ExecutorService executor = Executors.newFixedThreadPool(uploads)) {
      results = executor.invokeAll(IntStream.range(0, uploads).mapToObj(i -> upload).toList());
    }

    Set<String> paths = results.stream().map(this::get).collect(Collectors.toSet());
    assertEquals(uploads, paths.size());
  }

  private String get(Future<String> future) {
    try {
      return future.get();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
