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

import java.nio.ByteBuffer;

public class DatabaseWrapper {

	public static native int getEnvInfoSize();

	public static native int getStatSize();

	public static native long envCreate();

	public static native void envOpen(long env, String path, int flags, int mode);

	public static native void envCopy(long env, String path);

	public static native void envStat(long env, ByteBuffer stat);

	public static native void envInfo(long env, ByteBuffer info);

	public static native void envSync(long env, boolean force);

	public static native void envClose(long env);

	public static native void envSetFlags(long env, int flags, boolean onoff);

	public static native int envGetFlags(long env);

	public static native String envGetPath(long env);

	public static native void envSetMapSize(long env, long size);

	public static native void envSetMaxReaders(long env, int readers);

	public static native int envGetMaxReaders(long env);

	public static native void envSetMaxDbs(long env, int dbs);

	public static native int envGetMaxKeySize(long env);

	public static native long txnBegin(long env, long parent, int flags);

	public static native long txnEnv(long txn);

	public static native void txnCommit(long txn);

	public static native void txnAbort(long txn);

	public static native void txnReset(long txn);

	public static native void txnRenew(long txn);

	public static native int dbiOpen(long txn, String name, int flags);

	public static native void stat(long txn, int dbi, ByteBuffer stat);

	public static native int dbiFlags(long txn, int dbi);

	public static native void dbiClose(long env, int dbi);

	// TODO custom compare/dupsort/relfunc/relctx

	public static native void get(long txn, int dbi, byte[] key, int kofs,
			int klen, byte[] value, int vofs, int vlen);

	public static native void put(long txn, int dbi, byte[] key, int kofs,
			int klen, byte[] value, int vofs, int vlen, int flags);

	public static native void del(long txn, int dbi, byte[] key, int kofs,
			int klen, byte[] value, int vofs, int vlen);

	public static native long cursorOpen(long txn, int dbi);

	public static native void cursorClose(long cursor);

	public static native void cursorRenew(long txn, long cursor);

	public static native long cursorTxn(long cursor);

	public static native int cursorDbi(long cursor);

	public static native void cursorGet(long cursor, byte[] key, int kofs,
			int klen, byte[] value, int vofs, int vlen, int op);

	public static native void cursorPut(long cursor, byte[] key, int kofs,
			int klen, byte[] value, int vofs, int vlen, int flags);

	public static native void cursorDel(long cursor, int flags);

	// TODO mdb_cmp/mdb_dcmp/mdb_reader_list/mdb_reader_check
}
