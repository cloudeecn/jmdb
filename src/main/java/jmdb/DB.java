package jmdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DB {
	private static final Logger log = LoggerFactory.getLogger(DB.class);

	final Env env;
	final Transaction transaction;
	final int handle;
	final String name;

	private boolean closed = false;
	private StackTraceElement[] trace;

	DB(Transaction transaction, String name, int flags) {
		this.transaction = transaction;
		env = transaction.env;
		this.name = name;
		if (env.traceCreation) {
			try {
				throw new RuntimeException();
			} catch (RuntimeException e) {
				trace = e.getStackTrace();
			}
		}
		handle = DatabaseWrapper.dbiOpen(transaction.pointer, name, flags);
	}

	public int getFlags() {
		if (closed) {
			throw new IllegalStateException("DB is closed");
		} else {
			return DatabaseWrapper.dbiFlags(transaction.pointer, handle);
		}
	}

	private void checkParameters(String msg, byte[] buf, int ofs, int len) {
		if (buf == null) {
			throw new NullPointerException(msg);
		}
		if (ofs < 0 || len < 0 || ofs + len > buf.length) {
			throw new IndexOutOfBoundsException("msg: " + ofs + "-"
					+ (ofs + len) + "/" + buf.length);
		}
	}

	public int get(byte[] key, int kofs, int klen, byte[] value, int vofs,
			int vlen) {
		if (closed) {
			throw new IllegalStateException("DB is closed");
		} else {
			checkParameters("key", key, kofs, klen);
			checkParameters("value", value, vofs, vlen);
			return DatabaseWrapper.get(transaction.pointer, handle, key, kofs,
					klen, value, vofs, vlen);
		}
	}

	public void put(byte[] key, int kofs, int klen, byte[] value, int vofs,
			int vlen, int flags) {
		if (closed) {
			throw new IllegalStateException("DB is closed");
		} else {
			checkParameters("key", key, kofs, klen);
			checkParameters("value", value, vofs, vlen);
			DatabaseWrapper.put(transaction.pointer, handle, key, kofs, klen,
					value, vofs, vlen, flags);
		}
	}

	public boolean remove(byte[] key, int kofs, int klen, byte[] value,
			int vofs, int vlen) {
		if (closed) {
			throw new IllegalStateException("DB is closed");
		} else {
			checkParameters("key", key, kofs, klen);
			if (value != null) {
				checkParameters("value", value, vofs, vlen);
			}
			return DatabaseWrapper.del(transaction.pointer, handle, key, kofs,
					klen, value, vofs, vlen);
		}
	}

	void close() {
		if (!closed) {
			DatabaseWrapper.dbiClose(env.pointer, handle);
			transaction.unregisterDB(this);
			closed = true;
		}
	}

	protected void finalize() {
		if (!closed) {
			log.error("DB not closed!!!");
			if (trace != null) {
				for (StackTraceElement t : trace) {
					log.error("\t" + t.toString());
				}
			}
			close();
		}
	}
}
