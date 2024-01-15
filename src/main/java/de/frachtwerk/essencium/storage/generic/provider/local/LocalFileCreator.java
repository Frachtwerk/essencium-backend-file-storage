/*
 * Copyright (C) 2024 Frachtwerk GmbH, Leopoldstraße 7C, 76133 Karlsruhe.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalFileCreator {
  private static final Logger LOG = LoggerFactory.getLogger(LocalFileCreator.class);

  private final LocalStorageConfiguration config;

  @EventListener(ApplicationReadyEvent.class)
  public void init() throws IOException {
    if (config.isActive() && !config.isUseTempFiles()) {
      Path path = config.resolvePath();

      if (!Files.exists(path)) {
        LOG.debug("Directory {} for storing files does not exist.", path);
        Files.createDirectories(path);
        LOG.info("Created directory for file storing.");
      }
    }
  }

  public File createFile(String name) throws IOException {
    if (config.isUseTempFiles()) {
      return File.createTempFile(name, null);
    } else {
      Path pre = Path.of(config.resolvePath().toString(), name);
      Path path = Files.createFile(pre);
      return path.toFile();
    }
  }
}
