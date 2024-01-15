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

package de.frachtwerk.essencium.storage.impl.uuid.provider.s3;

import de.frachtwerk.essencium.storage.generic.model.StorageInfoVisitor;
import de.frachtwerk.essencium.storage.generic.provider.s3.AbstractS3StorageInfo;
import de.frachtwerk.essencium.storage.impl.uuid.model.UUIDFile;
import de.frachtwerk.essencium.storage.impl.uuid.model.UUIDStorageInfo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
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
public class S3UUIDStorageInfo extends UUIDStorageInfo
    implements AbstractS3StorageInfo<UUIDFile, UUID, UUIDStorageInfo> {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull private String s3ObjectKey;

  public S3UUIDStorageInfo(UUIDFile file, @NotNull String s3ObjectKey) {
    super(file);
    this.s3ObjectKey = s3ObjectKey;
  }

  @Override
  public <T> T accept(StorageInfoVisitor<T, UUIDFile, UUID, UUIDStorageInfo> visitor) {
    return visitor.visit(this);
  }
}
