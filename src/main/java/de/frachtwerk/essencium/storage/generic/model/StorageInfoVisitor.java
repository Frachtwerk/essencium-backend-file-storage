/*
 * Copyright (C) 2023 Frachtwerk GmbH, Leopoldstraße 7C, 76133 Karlsruhe.
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

import de.frachtwerk.essencium.storage.generic.provider.local.AbstractLocalStorageInfo;
import de.frachtwerk.essencium.storage.generic.provider.s3.AbstractS3StorageInfo;
import java.io.Serializable;
import java.util.function.Function;

public interface StorageInfoVisitor<
    T,
    F extends AbstractFile<F, ID, S>,
    ID extends Serializable,
    S extends AbstractStorageInfo<F, ID, S>> {
  T visit(AbstractLocalStorageInfo<F, ID, S> info);

  T visit(AbstractS3StorageInfo<F, ID, S> info);

  static <
          T,
          F extends AbstractFile<F, ID, S>,
          ID extends Serializable,
          S extends AbstractStorageInfo<F, ID, S>>
      StorageInfoVisitor<T, F, ID, S> local(
          Function<AbstractLocalStorageInfo<F, ID, S>, T> visitor) {
    return new Empty<>() {
      @Override
      public T visit(AbstractLocalStorageInfo<F, ID, S> info) {
        return visitor.apply(info);
      }
    };
  }

  static <
          T,
          F extends AbstractFile<F, ID, S>,
          ID extends Serializable,
          S extends AbstractStorageInfo<F, ID, S>>
      StorageInfoVisitor<T, F, ID, S> s3(Function<AbstractS3StorageInfo<F, ID, S>, T> visitor) {
    return new Empty<>() {
      @Override
      public T visit(AbstractS3StorageInfo<F, ID, S> info) {
        return visitor.apply(info);
      }
    };
  }

  class Empty<
          T,
          F extends AbstractFile<F, ID, S>,
          ID extends Serializable,
          S extends AbstractStorageInfo<F, ID, S>>
      implements StorageInfoVisitor<T, F, ID, S> {
    @Override
    public T visit(AbstractLocalStorageInfo<F, ID, S> info) {
      return null;
    }

    @Override
    public T visit(AbstractS3StorageInfo<F, ID, S> info) {
      return null;
    }
  }
}
