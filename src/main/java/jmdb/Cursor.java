package jmdb;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cursor implements Closeable {
	private static final Logger log = LoggerFactory.getLogger(Cursor.class);

	final long pointer;
	final DB db;
	final Transaction transaction;
	final Env env;

	private boolean closed = false;
	private StackTraceElement[] trace;

	private void checkClosed() {
		if (closed) {
			throw new IllegalStateException("DB closed");
		}
	}

	public Cursor(DB db) {
		this.db = db;
		this.transaction = db.transaction;
		this.env = transaction.env;
		this.pointer = DatabaseWrapper.cursorOpen(transaction.pointer,
				db.handle);

		if (env.traceCreation) {
			try {
				throw new RuntimeException();
			} catch (RuntimeException e) {
				trace = e.getStackTrace();
			}
		}
	}

	@Override
	public void close() {
		if (!closed) {
			DatabaseWrapper.cursorClose(pointer);
			closed = true;
		}
	}

	public long get(byte[] key, int kofs, int klen, byte[] value, int vofs,
			int vlen, CursorOp op) {
		checkClosed();
		return DatabaseWrapper.cursorGet(pointer, key, kofs, klen, value, vofs,
				vlen, op.getCode());
	}

	public void get(byte[] key, int kofs, int klen, byte[] value, int vofs,
			int vlen, CursorOp op, Sizes sizes) {
		checkClosed();
		long result = DatabaseWrapper.cursorGet(pointer, key, kofs, klen,
				value, vofs, vlen, op.getCode());
		if (sizes != null) {
			int keySize = (int) (result >>> 32);
			int valueSize = (int) (result);
			sizes.keySize = keySize;
			sizes.valueSize = valueSize;
		}
	}

	public void remove() {
		remove(false);
	}

	public void remove(boolean allSameKey) {
		checkClosed();
		DatabaseWrapper.cursorDel(pointer,
				allSameKey ? WriteFlags.MDB_NODUPDATA : 0);
	}

	protected void finalize() {
		if (!closed) {
			log.error("Cursor not closed!!!");
			if (trace != null) {
				for (StackTraceElement t : trace) {
					log.error("\t" + t.toString());
				}
			}
		}
	}
}
