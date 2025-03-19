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

package de.frachtwerk.essencium.storage.generic.provider.local;

import de.frachtwerk.essencium.storage.generic.model.AbstractFile;
import de.frachtwerk.essencium.storage.generic.model.AbstractStorageInfo;
import de.frachtwerk.essencium.storage.generic.model.StorageInfoVisitor;
import java.io.Serializable;
import org.springframework.core.io.Resource;

public interface AbstractLocalStorageInfo<
    F extends AbstractFile<F, ID, S>,
    ID extends Serializable,
    S extends AbstractStorageInfo<F, ID, S>> {

  <T> T accept(StorageInfoVisitor<T, F, ID, S> visitor);

  String getPath();

  F getFile();

  void setAvailable(boolean available);

  void setContent(Resource content);
}
