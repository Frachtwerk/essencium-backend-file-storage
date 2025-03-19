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

package de.frachtwerk.essencium.storage.generic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.frachtwerk.essencium.backend.model.AbstractBaseModel;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.core.io.Resource;

@MappedSuperclass
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public abstract class AbstractStorageInfo<
        F extends AbstractFile<F, ID, S>,
        ID extends Serializable,
        S extends AbstractStorageInfo<F, ID, S>>
    extends AbstractBaseModel<ID> {

  @ManyToOne @JsonIgnore @EqualsAndHashCode.Exclude @ToString.Exclude private F file;
  @Builder.Default private boolean available = true;
  @Transient @JsonIgnore private Resource content;

  public AbstractStorageInfo(F file) {
    this.file = file;
  }

  public abstract <T> T accept(StorageInfoVisitor<T, F, ID, S> visitor);
}
