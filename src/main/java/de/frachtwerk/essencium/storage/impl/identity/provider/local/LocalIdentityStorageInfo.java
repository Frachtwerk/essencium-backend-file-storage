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

package de.frachtwerk.essencium.storage.impl.identity.provider.local;

import de.frachtwerk.essencium.storage.generic.provider.local.AbstractLocalStorageInfo;
import de.frachtwerk.essencium.storage.impl.identity.model.IdentityFile;
import de.frachtwerk.essencium.storage.impl.identity.model.IdentityStorageInfo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Entity(name = "LOCAL_STORAGE_INFO")
@NoArgsConstructor
@Data
@SuperBuilder(toBuilder = true)
public class LocalIdentityStorageInfo extends IdentityStorageInfo
    implements AbstractLocalStorageInfo<IdentityFile, Long, IdentityStorageInfo> {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull private String path;

  public LocalIdentityStorageInfo(IdentityFile file, @NotNull String path) {
    super(file);
    this.path = path;
  }
}
