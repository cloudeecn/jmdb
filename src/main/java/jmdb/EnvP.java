/**
 *  Copyright 2014-2014 Yuxuan Huang. All rights reserved.
 *
 * This file is part of jmdb
 *
 * jmdb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * jmdb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jmdb. If not, see <http://www.gnu.org/licenses/>.
 */
package jmdb;

import java.io.Closeable;

public class EnvP implements Closeable {

	private long pointer;

	public EnvP() {
		pointer = DatabaseWrapper.envCreate();
	}

	public void open(String path, int flags, int mode) {
		DatabaseWrapper.envOpen(pointer, path, flags, mode);
	}

	public void close() {
		DatabaseWrapper.envClose(pointer);
	}

}
