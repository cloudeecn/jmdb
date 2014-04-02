package jmdb;

import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transaction {

	private static final Logger log = LoggerFactory
			.getLogger(Transaction.class);

	final Env env;
	final long pointer;
	final boolean readOnly;

	private boolean closed = false;
	private StackTraceElement[] trace;
	private final HashSet<DB> openedDbs = new HashSet<DB>();

	private void checkClosed() {
		if (closed) {
			throw new IllegalStateException("Transaction closed");
		}
	}

	Transaction(Env env, Transaction parent, boolean readOnly) {
		if (env.traceCreation) {
			try {
				throw new RuntimeException();
			} catch (RuntimeException e) {
				trace = e.getStackTrace();
			}
		}
		this.readOnly = readOnly;
		this.env = env;
		pointer = DatabaseWrapper.txnBegin(env.pointer, parent == null ? 0
				: parent.pointer, readOnly ? EnvFlags.MDB_RDONLY : 0);
	}

	public void commit() {
		checkClosed();
		DatabaseWrapper.txnCommit(pointer);
		close();
	}

	public void rollback() {
		if (!closed) {
			DatabaseWrapper.txnAbort(pointer);
			close();
		}
	}

	private void close() {
		if (log.isDebugEnabled()) {
			log.debug("Closing unclosed dbs ({})", openedDbs.size());
		}
		ArrayList<DB> toClose = new ArrayList<DB>(openedDbs);
		for (DB db : toClose) {
			db.close();
		}
		closed = true;
	}

	public Transaction childTransaction(boolean readOnly) {
		checkClosed();
		return new Transaction(env, this, readOnly);
	}

	public DB openDB(String name, int flags) {
		checkClosed();
		DB db = new DB(this, name, flags);
		openedDbs.add(db);
		return db;
	}

	void unregisterDB(DB db) {
		openedDbs.remove(db);
	}

	protected void finalize() {
		if (!closed) {
			log.error("Transaction left without committed/rollbacked!!!");
			if (trace != null) {
				for (StackTraceElement t : trace) {
					log.error("\t" + t.toString());
				}
			}
			rollback();
		}
	}

}
