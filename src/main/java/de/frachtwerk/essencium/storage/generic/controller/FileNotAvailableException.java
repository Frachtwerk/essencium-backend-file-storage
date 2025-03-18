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

package de.frachtwerk.essencium.storage.generic.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class FileNotAvailableException extends RuntimeException {
  public FileNotAvailableException() {
    super();
  }

  public FileNotAvailableException(String message) {
    super(message);
  }

  public FileNotAvailableException(Long id) {
    super("File " + id.toString() + " is not present on disk");
  }

  public FileNotAvailableException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileNotAvailableException(Long id, Throwable cause) {
    super("File " + id.toString() + " is not present on disk", cause);
  }
}
