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

public final class Flags {
	public static final int MDB_FIXEDMAP = 0x01;
	public static final int MDB_NOSUBDIR = 0x4000;
	public static final int MDB_NOSYNC = 0x10000;
	public static final int MDB_RDONLY = 0x20000;
	public static final int MDB_NOMETASYNC = 0x40000;
	public static final int MDB_WRITEMAP = 0x80000;
	public static final int MDB_MAPASYNC = 0x100000;
	public static final int MDB_NOTLS = 0x200000;
	public static final int MDB_NOLOCK = 0x400000;
	public static final int MDB_NORDAHEAD = 0x800000;
	public static final int MDB_NOMEMINIT = 0x1000000;
}
