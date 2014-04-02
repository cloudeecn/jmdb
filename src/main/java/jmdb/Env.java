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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Env implements Closeable {

	private final static Logger log = LoggerFactory.getLogger(Env.class);

	final long pointer;
	final boolean traceCreation;

	private boolean closed;
	private String path;
	private StackTraceElement[] trace;

	public Env() {
		this(false);
	}

	private void checkClosed() {
		if (closed) {
			throw new IllegalStateException("Env closed");
		}
	}

	public Env(boolean traceCreation) {
		this.traceCreation = traceCreation;
		if (traceCreation) {
			try {
				throw new RuntimeException();
			} catch (RuntimeException e) {
				trace = e.getStackTrace();
			}
		}
		pointer = DatabaseWrapper.envCreate();
	}

	public void setMapSize(long mapSize) {
		checkClosed();
		DatabaseWrapper.envSetMapSize(pointer, mapSize);
	}

	public void setMaxReaders(int readers) {
		checkClosed();
		DatabaseWrapper.envSetMaxReaders(pointer, readers);
	}

	public void setMaxDbs(int dbs) {
		checkClosed();
		DatabaseWrapper.envSetMaxDbs(pointer, dbs);
	}

	public int getMaxReaders() {
		checkClosed();
		return DatabaseWrapper.envGetMaxReaders(pointer);
	}

	public int getMaxKeySize() {
		checkClosed();
		return DatabaseWrapper.envGetMaxKeySize(pointer);
	}

	public void open(String path, int flags, int mode) {
		checkClosed();
		this.path = path;
		DatabaseWrapper.envOpen(pointer, path, flags, mode);
	}

	public void setFlags(int flags, boolean onoff) {
		checkClosed();
		DatabaseWrapper.envSetFlags(pointer, flags, onoff);
	}

	public int getFlags() {
		checkClosed();
		return DatabaseWrapper.envGetFlags(pointer);
	}

	public Transaction beginTransaction(boolean readOnly) {
		checkClosed();
		return new Transaction(this, null, readOnly);
	}

	public void sync(boolean force) {
		checkClosed();
		DatabaseWrapper.envSync(pointer, force);
	}

	public void close() {
		if (!closed) {
			DatabaseWrapper.envClose(pointer);
			closed = true;
		}
	}

	public String getPath() {
		return path;
	}

	protected void finalize() {
		if (!closed) {
			log.error("Env not closed!!!");
			if (trace != null) {
				for (StackTraceElement t : trace) {
					log.error("\t" + t.toString());
				}
			}
			close();
		}
	}

}
