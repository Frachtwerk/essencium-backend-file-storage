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

import de.frachtwerk.essencium.storage.generic.model.AbstractStorageInfo;
import de.frachtwerk.essencium.storage.generic.model.StorageInfoVisitor;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity(name = "STORAGE_INFO")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SuperBuilder(toBuilder = true)
public class IdentityStorageInfo
    extends AbstractStorageInfo<IdentityFile, Long, IdentityStorageInfo> {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public IdentityStorageInfo(IdentityFile file) {
    super(file);
  }

  @Override
  public <T> T accept(StorageInfoVisitor<T, IdentityFile, Long, IdentityStorageInfo> visitor) {
    return null;
  }

  @Override
  public String getTitle() {
    return "IdentityStorageInfo " + id;
  }
}
