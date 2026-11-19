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
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class MimeTypeHelper {
  private static final Pattern EXTENSION = Pattern.compile("[A-Za-z0-9]+");

  public String getMimeType(String name, byte[] fileContent) throws IOException {
    // The temporary file only takes the extension, so the name can not select its location.
    Path file = Files.createTempFile("upload", extensionOf(name));
    try {
      Files.write(file, fileContent);
      return Files.probeContentType(file);
    } finally {
      Files.deleteIfExists(file);
    }
  }

  private static String extensionOf(String name) {
    if (name == null || name.lastIndexOf('.') < 0) {
      return null;
    }
    String extension = name.substring(name.lastIndexOf('.') + 1);
    return EXTENSION.matcher(extension).matches() ? "." + extension : null;
  }
}
