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
	jmethodID initMethod = (*vm)->GetMethodID(vm, exceptionClazz, "<void>",
			"(I)v");
	jobject obj = (*vm)->NewObject(vm, exceptionClazz, initMethod, code);
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
	int ret = mdb_env_create(&env);
	if (ret != 0) {
		throwDatabaseException(vm, ret);
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
	int ret = mdb_env_open(envC, pathC, flags, mode);
	(*vm)->ReleaseStringUTFChars(vm, path, pathC);
	if (ret != 0) {
		throwDatabaseException(vm, ret);
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
	int ret = mdb_env_copy(envC, pathC);
	(*vm)->ReleaseStringUTFChars(vm, path, pathC);
	if (ret != 0) {
		throwDatabaseException(vm, ret);
	}
}
