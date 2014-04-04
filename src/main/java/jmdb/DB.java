package jmdb;

import java.io.UnsupportedEncodingException;

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

	private void checkClosed() {
		if (closed) {
			throw new IllegalStateException("DB is closed");
		}
	}

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
		checkClosed();
		return DatabaseWrapper.dbiFlags(transaction.pointer, handle);
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
		checkClosed();
		checkParameters("key", key, kofs, klen);
		checkParameters("value", value, vofs, vlen);
		return DatabaseWrapper.get(transaction.pointer, handle, key, kofs,
				klen, value, vofs, vlen);
	}

	public int get(byte[] key, byte[] holder) {
		return get(key, 0, key.length, holder, 0, holder.length);
	}

	public byte[] get(byte[] key, int maxValueSize) {
		byte[] holder = new byte[maxValueSize];
		int size = get(key, 0, key.length, holder, 0, holder.length);
		byte[] ret;
		if (size != maxValueSize) {
			ret = new byte[size];
			System.arraycopy(holder, 0, ret, 0, size);
		} else {
			ret = holder;
		}
		return ret;
	}

	public void put(byte[] key, int kofs, int klen, byte[] value, int vofs,
			int vlen, int flags) {
		checkClosed();
		checkParameters("key", key, kofs, klen);
		checkParameters("value", value, vofs, vlen);
		DatabaseWrapper.put(transaction.pointer, handle, key, kofs, klen,
				value, vofs, vlen, flags);
	}

	public void put(byte[] key, byte[] value) {
		put(key, 0, key.length, value, 0, value.length, 0);
	}

	public void put(String key, String value) {
		try {
			put(key.getBytes("utf-8"), value.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			// not likely to be happened
		}
	}

	public boolean remove(byte[] key, int kofs, int klen, byte[] value,
			int vofs, int vlen) {
		checkClosed();
		checkParameters("key", key, kofs, klen);
		if (value != null) {
			checkParameters("value", value, vofs, vlen);
		}
		return DatabaseWrapper.del(transaction.pointer, handle, key, kofs,
				klen, value, vofs, vlen);
	}

	public Cursor openCursor() {
		checkClosed();
		return new Cursor(this);
	}

	public Stat getStat() {
		checkClosed();
		Stat ret = new Stat();
		DatabaseWrapper.stat(transaction.pointer, this.handle, ret.values);
		return ret;
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
