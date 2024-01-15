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

package de.frachtwerk.essencium.storage.impl.identity.provider.s3;

import de.frachtwerk.essencium.storage.generic.provider.s3.AbstractS3StorageInfo;
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
@Entity(name = "S3_STORAGE_INFO")
@NoArgsConstructor
@Data
@SuperBuilder(toBuilder = true)
public class S3IdentityStorageInfo extends IdentityStorageInfo
    implements AbstractS3StorageInfo<IdentityFile, Long, IdentityStorageInfo> {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull private String s3ObjectKey;

  public S3IdentityStorageInfo(IdentityFile file, @NotNull String s3ObjectKey) {
    super(file);
    this.s3ObjectKey = s3ObjectKey;
  }
}
