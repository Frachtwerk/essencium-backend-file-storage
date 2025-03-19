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

import java.io.IOException;
import java.nio.file.Files;
import org.springframework.stereotype.Component;

@Component
public class MimeTypeHelper {

  public String getMimeType(String name, byte[] fileContent) throws IOException {
    String mimeType;
    final java.io.File folder = Files.createTempDirectory("starter").toFile();
    folder.deleteOnExit();
    java.io.File f = new java.io.File(folder, name);
    try {
      Files.write(f.toPath(), fileContent);
      mimeType = Files.probeContentType(f.toPath());
    } finally {
      f.delete();
    }
    return mimeType;
  }
}
