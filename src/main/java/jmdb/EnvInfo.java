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


public class EnvInfo {
	long[] values;

	public EnvInfo() {
		values = new long[DatabaseWrapper.getStatSize()];
	}

	public long getMapAddr() {
		return values[0];
	}

	public long getMapSize() {
		return values[1];
	}

	public long getLastPageNo() {
		return values[2];
	}

	public long getLastTransactionId() {
		return values[3];
	}

	public long getMaxReaders() {
		return values[4];
	}

	public long getNumReaders() {
		return values[5];
	}
}
