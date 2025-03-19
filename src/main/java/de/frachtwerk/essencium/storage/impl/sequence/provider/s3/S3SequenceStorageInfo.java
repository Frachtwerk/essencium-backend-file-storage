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

package de.frachtwerk.essencium.storage.impl.sequence.provider.s3;

import de.frachtwerk.essencium.storage.generic.model.StorageInfoVisitor;
import de.frachtwerk.essencium.storage.generic.provider.s3.AbstractS3StorageInfo;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceFile;
import de.frachtwerk.essencium.storage.impl.sequence.model.SequenceStorageInfo;
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
public class S3SequenceStorageInfo extends SequenceStorageInfo
    implements AbstractS3StorageInfo<SequenceFile, Long, SequenceStorageInfo> {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
  @SequenceGenerator(
      name = "hibernate_sequence",
      sequenceName = "hibernate_sequence",
      allocationSize = 1)
  private Long id;

  @NotNull private String s3ObjectKey;

  public S3SequenceStorageInfo(SequenceFile file, @NotNull String s3ObjectKey) {
    super(file);
    this.s3ObjectKey = s3ObjectKey;
  }

  @Override
  public <T> T accept(StorageInfoVisitor<T, SequenceFile, Long, SequenceStorageInfo> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String getTitle() {
    return "S3SequenceStorageInfo " + id;
  }
}
