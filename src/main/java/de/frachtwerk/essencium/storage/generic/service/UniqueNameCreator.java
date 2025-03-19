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

import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UniqueNameCreator {
  @NotNull
  public String getUniqueName(String originalName, Predicate<String> exists) {
    String name;
    int i = 1;
    name = originalName;
    final List<String> parts = Arrays.asList(originalName.split("\\."));
    String suffix = parts.get(parts.size() - 1);
    String prefix = parts.stream().limit(parts.size() - 1).collect(Collectors.joining("."));

    while (exists.test(name)) {
      name = prefix + "_" + i++ + "." + suffix;
    }
    return name;
  }
}
