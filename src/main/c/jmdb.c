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
#include "jmdb.h"
#include "lmdb.h"

#ifndef NULL
# define NULL ((void*)(0x0))
#endif

static void throwDatabaseException(JNIEnv *vm, jint code) {
	jclass exceptionClazz = (*vm)->FindClass(vm, "jmdb/DatabaseException");
	jmethodID initMethod = (*vm)->GetMethodID(vm, exceptionClazz, "<init>",
			"(I)V");
	jobject obj = (*vm)->NewObject(vm, exceptionClazz, initMethod, code);
	(*vm)->Throw(vm, obj);
}

static void throwNew(JNIEnv *vm, const char *className, const char *message) {
	jobject obj;
	jmethodID initMethod;
	jobject msg;
	jclass exceptionClazz = (*vm)->FindClass(vm, className);
	if ((*vm)->ExceptionOccurred(vm)) {
		return;
	}
	if (message) {
		initMethod = (*vm)->GetMethodID(vm, exceptionClazz, "<init>",
				"(Ljava/lang/String;)V");
		if ((*vm)->ExceptionOccurred(vm)) {
			return;
		}
		msg = (*vm)->NewStringUTF(vm, message);
		if ((*vm)->ExceptionOccurred(vm)) {
			return;
		}
		obj = (*vm)->NewObject(vm, exceptionClazz, initMethod, msg);
	} else {
		initMethod = (*vm)->GetMethodID(vm, exceptionClazz, "<init>", "()V");
		if ((*vm)->ExceptionOccurred(vm)) {
			return;
		}
		obj = (*vm)->NewObject(vm, exceptionClazz, initMethod);
	}
	(*vm)->Throw(vm, obj);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    getEnvSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jmdb_DatabaseWrapper_getEnvInfoSize(JNIEnv *vm,
		jclass clazz) {
	return sizeof(MDB_envinfo);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    getStatSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jmdb_DatabaseWrapper_getStatSize(JNIEnv *vm,
		jclass clazz) {
	return sizeof(MDB_stat);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envCreate
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_jmdb_DatabaseWrapper_envCreate(JNIEnv *vm,
		jclass clazz) {
	MDB_env *env;
	int code = mdb_env_create(&env);
	if (code != 0) {
		throwDatabaseException(vm, code);
		return 0;
	}
	return (jlong) env;
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envOpen
 * Signature: (JLjava/lang/String;II)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_envOpen(JNIEnv *vm,
		jclass clazz, jlong envL, jstring path, jint flags, jint mode) {
	MDB_env *envC = (MDB_env*) envL;
	const char *pathC = (*vm)->GetStringUTFChars(vm, path, NULL);
	int code = mdb_env_open(envC, pathC, flags, mode);
	(*vm)->ReleaseStringUTFChars(vm, path, pathC);
	if (code != 0) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envCopy
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_envCopy(JNIEnv *vm,
		jclass clazz, jlong envL, jstring path) {
	MDB_env *envC = (MDB_env*) envL;
	const char *pathC = (*vm)->GetStringUTFChars(vm, path, NULL);
	int code = mdb_env_copy(envC, pathC);
	(*vm)->ReleaseStringUTFChars(vm, path, pathC);
	if (code != 0) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envStat
 * Signature: (JLjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_envStat(JNIEnv *vm,
		jclass clazz, jlong envL, jobject buf) {
	MDB_env *envC = (MDB_env*) envL;
	MDB_stat *stat = (*vm)->GetDirectBufferAddress(vm, buf);
	jlong size = (*vm)->GetDirectBufferCapacity(vm, buf);
	if (size < sizeof(MDB_stat)) {
		throwNew(vm, "java/lang/IndexOutOfBoundsException",
				"ByteBuffer's capacity is less than MDB_stat's size");
		return;
	}
	int code = mdb_env_stat(envC, stat);
	if (code) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envInfo
 * Signature: (JLjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_envInfo(JNIEnv *vm,
		jclass clazz, jlong envL, jobject buf) {
	MDB_env *envC = (MDB_env*) envL;
	MDB_envinfo *info = (*vm)->GetDirectBufferAddress(vm, buf);
	jlong size = (*vm)->GetDirectBufferCapacity(vm, buf);
	if (size < sizeof(MDB_envinfo)) {
		throwNew(vm, "java/lang/IndexOutOfBoundsException",
				"ByteBuffer's capacity is less than MDB_envinfo's size");
		return;
	}
	int code = mdb_env_info(envC, info);
	if (code) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envSync
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_envSync(JNIEnv *vm,
		jclass clazz, jlong envL, jboolean force) {
	MDB_env *envC = (MDB_env*) envL;
	int code = mdb_env_sync(envC, force);
	if (code) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envClose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_envClose(JNIEnv *vm,
		jclass clazz, jlong envL) {
	MDB_env *envC = (MDB_env*) envL;
	mdb_env_close(envC);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envSetFlags
 * Signature: (JIZ)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_envSetFlags(JNIEnv *vm,
		jclass clazz, jlong envL, jint flags, jboolean onoff) {
	MDB_env *envC = (MDB_env*) envL;
	int code = mdb_env_set_flags(envC, flags, onoff);
	if (code) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envGetFlags
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_jmdb_DatabaseWrapper_envGetFlags(JNIEnv *vm,
		jclass clazz, jlong envL) {
	MDB_env *envC = (MDB_env*) envL;
	unsigned int ret = 0;
	int code = mdb_env_get_flags(envC, &ret);
	if (code) {
		throwDatabaseException(vm, code);
		return 0;
	}
	return (jint) ret;
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envGetPath
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jmdb_DatabaseWrapper_envGetPath(JNIEnv *vm,
		jclass clazz, jlong envL) {
	MDB_env *envC = (MDB_env*) envL;
	char *ret;
	int code = mdb_env_get_path(envC, &ret);
	if (code) {
		throwDatabaseException(vm, code);
		return 0;
	}
	return (*vm)->NewStringUTF(vm, ret);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envSetMapSize
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_envSetMapSize(JNIEnv *vm,
		jclass clazz, jlong envL, jlong size) {
	MDB_env *envC = (MDB_env*) envL;
	int code = mdb_env_set_mapsize(envC, size);
	if (code) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envSetMaxReaders
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_envSetMaxReaders(JNIEnv *vm,
		jclass clazz, jlong envL, jint readers) {
	MDB_env *envC = (MDB_env*) envL;
	int code = mdb_env_set_maxreaders(envC, readers);
	if (code) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envGetMaxReaders
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_jmdb_DatabaseWrapper_envGetMaxReaders(JNIEnv *vm,
		jclass clazz, jlong envL) {
	MDB_env *envC = (MDB_env*) envL;
	unsigned int max_readers;
	int code = mdb_env_get_maxreaders(envC, &max_readers);
	if (code) {
		throwDatabaseException(vm, code);
	}
	return max_readers;
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envSetMaxDbs
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_envSetMaxDbs(JNIEnv *vm,
		jclass clazz, jlong envL, jint dbs) {
	MDB_env *envC = (MDB_env*) envL;
	int code = mdb_env_set_maxdbs(envC, dbs);
	if (code) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    envGetMaxKeySize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_jmdb_DatabaseWrapper_envGetMaxKeySize(JNIEnv *vm,
		jclass clazz, jlong envL) {
	MDB_env *envC = (MDB_env*) envL;
	return mdb_env_get_maxreaders(envC);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    txnBegin
 * Signature: (JJI)J
 */
JNIEXPORT jlong JNICALL Java_jmdb_DatabaseWrapper_txnBegin(JNIEnv *vm,
		jclass clazz, jlong envL, jlong parentL, jint flags) {
	MDB_env *envC = (MDB_env*) envL;
	MDB_txn *parentC = (MDB_txn *) parentL;
	MDB_txn *result;
	int code = mdb_txn_begin(envC, parentC, flags, &result);
	if (code) {
		throwDatabaseException(vm, code);
		return 0;
	}
	return (jlong) result;
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    txnEnv
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_jmdb_DatabaseWrapper_txnEnv(JNIEnv *vm,
		jclass clazz, jlong txnL) {
	MDB_env *env = mdb_txn_env((MDB_txn*) txnL);
	return (jlong) env;
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    txnCommit
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_txnCommit(JNIEnv *vm,
		jclass clazz, jlong txnL) {
	int code = mdb_txn_commit((MDB_txn*) txnL);
	if (code) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    txnAbort
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_txnAbort(JNIEnv *vm,
		jclass clazz, jlong txnL) {
	mdb_txn_abort((MDB_txn*) txnL);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    txnReset
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_txnReset(JNIEnv *vm,
		jclass clazz, jlong txnL) {
	mdb_txn_reset((MDB_txn*) txnL);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    txnRenew
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_txnRenew(JNIEnv *vm,
		jclass clazz, jlong txnL) {
	int code = mdb_txn_renew((MDB_txn*) txnL);
	if (code) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    dbiOpen
 * Signature: (JLjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_jmdb_DatabaseWrapper_dbiOpen(JNIEnv *vm,
		jclass clazz, jlong txnL, jstring name, jint flags) {
	MDB_txn *txnC = (MDB_txn*) txnL;
	const char *nameC = (*vm)->GetStringUTFChars(vm, name);
	MDB_dbi dbi;
	int code = mdb_dbi_open(txnC, nameC, flags, &dbi);
	(*vm)->ReleaseStringUTFChars(vm, name, nameC);
	if (code) {
		throwDatabaseException(vm, code);
		return -1;
	}
	return dbi;
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    stat
 * Signature: (JILjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_stat(JNIEnv *vm, jclass clazz,
		jlong txnL, jint dbi, jobject buf) {
	MDB_txn *txnC = (MDB_txn*) txnL;
	MDB_stat *stat = (*vm)->GetDirectBufferAddress(vm, buf);
	jlong size = (*vm)->GetDirectBufferCapacity(vm, buf);
	int code;
	if (size < sizeof(MDB_stat)) {
		throwNew(vm, "java/lang/IndexOutOfBoundsException",
				"ByteBuffer's capacity is less than MDB_stat's size");
		return;
	}
	code = mdb_stat(txnC, (MDB_dbi) dbi, stat);
	if (code) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    dbiFlags
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_jmdb_DatabaseWrapper_dbiFlags(JNIEnv *vm,
		jclass clazz, jlong txnL, jint dbi) {
	unsigned int flags;
	MDB_txn *txnC = (MDB_txn*) txnL;
	int code = mdb_dbi_flags(txnC, (MDB_dbi) dbi, &flags);
	if (code) {
		throwDatabaseException(vm, code);
		return 0;
	}
	return flags;
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    dbiClose
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_dbiClose(JNIEnv *vm,
		jclass clazz, jlong envL, jint dbi) {
	MDB_env *envC = (MDB_env*) envL;
	mdb_dbi_close(envC, (MDB_dbi) dbi);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    get
 * Signature: (JI[BII[BII)I
 */
JNIEXPORT jint JNICALL Java_jmdb_DatabaseWrapper_get(JNIEnv *vm, jclass clazz,
		jlong txnL, jint dbi, jbyteArray keyA, jint kofs, jint klen,
		jbyteArray valueA, jint vofs, jint vlen) {
	MDB_txn *txnC = (MDB_txn*) txnL;
	enum {
		NONE, OOM, MDB, IOOB
	} result = NONE;
	jint ret;
	char lenHolder[16];
	MDB_val key, value;

	jbyte *keyC = (*vm)->GetPrimitiveArrayCritical(vm, keyA, NULL);
	jbyte *valueC = (*vm)->GetPrimitiveArrayCritical(vm, valueA, NULL);
	if (keyC == NULL || valueC == NULL) {
		result = OOM;
	} else {
		key.mv_size = klen;
		key.mv_data = keyC + kofs;
		ret = mdb_get(txnC, (MDB_dbi) dbi, &key, &value);
		if (ret == MDB_NOTFOUND) {
			ret = -1;
		} else if (ret) {
			result = MDB;
		} else if (value.mv_size > vlen) {
			sprintf(lenHolder, "%d", value.mv_size);
			result = IOOB;
		} else {
			memcpy(valueC + vofs, value.mv_data, value.mv_size);
			ret = value.mv_size;
		}
	}
	(*vm)->ReleasePrimitiveArrayCritical(vm, valueA, valueC, 0);
	(*vm)->ReleasePrimitiveArrayCritical(vm, keyA, keyC, JNI_ABORT);

	switch (result) {
	case NONE:
		return ret;
	case OOM:
		throwNew(vm, "java/lang/OutOfMemoryError", "");
		return -1;
	case MDB:
		throwDatabaseException(vm, ret);
		return -1;
	case IOOB:
		throwNew(vm, "java/lang/IndexOutOfBoundException", lenHolder);
		return -1;
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    put
 * Signature: (JI[BII[BIII)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_put(JNIEnv *vm, jclass clazz,
		jlong txnL, jint dbi, jbyteArray keyA, jint kofs, jint klen,
		jbyteArray valueA, jint vofs, jint vlen, jint flags) {
	MDB_txn *txnC = (MDB_txn*) txnL;
	enum {
		NONE, OOM, MDB
	} result = NONE;
	jint ret;
	char lenHolder[16];
	MDB_val key, value;

	jbyte *keyC = (*vm)->GetPrimitiveArrayCritical(vm, keyA, NULL);
	jbyte *valueC = (*vm)->GetPrimitiveArrayCritical(vm, valueA, NULL);
	if (keyC == NULL || valueC == NULL) {
		result = OOM;
	} else {
		key.mv_size = klen;
		key.mv_data = keyC + kofs;
		value.mv_size = vlen;
		value.mv_data = valueC + vofs;
		ret = mdb_put(txnC, (MDB_dbi) dbi, &key, &value, (unsigned int) flags);
		if (ret) {
			result = MDB;
		}
	}
	(*vm)->ReleasePrimitiveArrayCritical(vm, valueA, valueC, 0);
	(*vm)->ReleasePrimitiveArrayCritical(vm, keyA, keyC, JNI_ABORT);

	switch (result) {
	case NONE:
		return;
	case OOM:
		throwNew(vm, "java/lang/OutOfMemoryError", "");
		return;
	case MDB:
		throwDatabaseException(vm, ret);
		return;
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    del
 * Signature: (JI[BII[BII)V
 */
JNIEXPORT jboolean JNICALL Java_jmdb_DatabaseWrapper_del(JNIEnv *vm,
		jclass clazz, jlong txnL, jint dbi, jbyteArray keyA, jint kofs,
		jint klen, jbyteArray valueA, jint vofs, jint vlen) {
	MDB_txn *txnC = (MDB_txn*) txnL;
	enum {
		NONE, OOM, MDB
	} result = NONE;
	jint ret;
	char lenHolder[16];
	MDB_val key, value;

	jbyte *keyC = (*vm)->GetPrimitiveArrayCritical(vm, keyA, NULL);
	jbyte *valueC =
			valueA ? (*vm)->GetPrimitiveArrayCritical(vm, valueA, NULL) : NULL;
	if (keyC == NULL || (valueA && valueC == NULL)) {
		result = OOM;
	} else {
		key.mv_size = klen;
		key.mv_data = keyC + kofs;
		if (valueA) {
			value.mv_size = vlen;
			value.mv_data = valueC + vofs;
			ret = mdb_del(txnC, (MDB_dbi) dbi, &key, &value);
		} else {
			ret = mdb_del(txnC, (MDB_dbi) dbi, &key, NULL);
		}
		if (ret == MDB_NOTFOUND) {
			ret = 0;
		} else if (ret) {
			result = MDB;
		} else {
			ret = 1;
		}
	}
	if (valueA) {
		(*vm)->ReleasePrimitiveArrayCritical(vm, valueA, valueC, 0);
	}
	(*vm)->ReleasePrimitiveArrayCritical(vm, keyA, keyC, JNI_ABORT);

	switch (result) {
	case NONE:
		return ret;
	case OOM:
		throwNew(vm, "java/lang/OutOfMemoryError", "");
		return -1;
	case MDB:
		throwDatabaseException(vm, ret);
		return -1;
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    cursorOpen
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_jmdb_DatabaseWrapper_cursorOpen(JNIEnv *vm,
		jclass clazz, jlong txnL, jint dbi) {
	MDB_txn *txnC = (MDB_txn*) txnL;
	MDB_cursor *cursorC;
	jint code;
	code = mdb_cursor_open(txnC, (MDB_dbi) dbi, &cursorC);
	if (code) {
		throwDatabaseException(vm, code);
	}
	return (jlong) cursorC;
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    cursorClose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_cursorClose(JNIEnv *vm,
		jclass clazz, jlong cursorL) {
	MDB_cursor *cursorC = (MDB_cursor*) cursorL;
	mdb_cursor_close(cursorC);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    cursorRenew
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_cursorRenew(JNIEnv *vm,
		jclass clazz, jlong txnL, jlong cursorL) {
	MDB_txn *txnC = (MDB_txn*) txnL;
	MDB_cursor *cursorC = (MDB_cursor*) cursorL;

	jint code = mdb_cursor_renew(txnC, cursorC);
	if (code) {
		throwDatabaseException(vm, code);
	}
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    cursorTxn
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_jmdb_DatabaseWrapper_cursorTxn(JNIEnv *vm,
		jclass clazz, jlong cursorL) {
	MDB_cursor *cursorC = (MDB_cursor*) cursorL;
	return (jlong) mdb_cursor_txn(cursorC);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    cursorDbi
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_jmdb_DatabaseWrapper_cursorDbi(JNIEnv *vm,
		jclass clazz, jlong cursorL) {
	MDB_cursor *cursorC = (MDB_cursor*) cursorL;
	return (jint) mdb_cursor_dbi(cursorC);
}

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    cursorGet
 * Signature: (J[BII[BIII)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_cursorGet(JNIEnv *vm,
		jclass clazz, jlong, jbyteArray, jint, jint, jbyteArray, jint, jint,
		jint);

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    cursorPut
 * Signature: (J[BII[BIII)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_cursorPut(JNIEnv *vm,
		jclass clazz, jlong, jbyteArray, jint, jint, jbyteArray, jint, jint,
		jint);

/*
 * Class:     jmdb_DatabaseWrapper
 * Method:    cursorDel
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_jmdb_DatabaseWrapper_cursorDel(JNIEnv *vm,
		jclass clazz, jlong, jint);
