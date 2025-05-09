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

package de.frachtwerk.essencium.storage.impl.identity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.frachtwerk.essencium.storage.generic.model.AbstractFile;
import de.frachtwerk.essencium.storage.generic.model.StorageInfoVisitor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity(name = "FILE")
@AllArgsConstructor
public class IdentityFile extends AbstractFile<IdentityFile, Long, IdentityStorageInfo> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @OrderColumn
  @OneToMany(cascade = CascadeType.ALL)
  List<IdentityStorageInfo> storageInfos;

  public IdentityFile(
      @NotNull List<IdentityStorageInfo> infos, String name, int length, String mimeType) {
    super(name, length, mimeType);
    this.storageInfos = infos;
  }

  public boolean isAvailable() {
    return storageInfos.stream().anyMatch(IdentityStorageInfo::isAvailable);
  }

  @JsonIgnore
  public Resource getContent() {
    return storageInfos.stream()
        .map(IdentityStorageInfo::getContent)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  public <T> Set<T> accept(StorageInfoVisitor<T, IdentityFile, Long, IdentityStorageInfo> visitor) {
    return storageInfos.stream().map(i -> i.accept(visitor)).collect(Collectors.toSet());
  }

  @Override
  public String getTitle() {
    return getName();
  }
}
