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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MimeTypeHelperTest {
  private final MimeTypeHelper mimeTypeHelper = new MimeTypeHelper();

  @Test
  void pathLikeNameDoesNotTouchFilesOutsideTheTemporaryDirectory() throws Exception {
    Path outsideFile = Files.createTempFile("mime-type-helper-test", ".txt");
    try {
      Files.writeString(outsideFile, "original");

      mimeTypeHelper.getMimeType(
          "../" + outsideFile.getFileName(), "uploaded".getBytes(StandardCharsets.UTF_8));

      assertTrue(Files.exists(outsideFile));
      assertEquals("original", Files.readString(outsideFile));
    } finally {
      Files.deleteIfExists(outsideFile);
    }
  }
}
