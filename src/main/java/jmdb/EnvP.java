package jmdb;

import java.io.Closeable;

public class EnvP implements Closeable {

	private long pointer;

	public EnvP() {
		pointer = DatabaseWrapper.envCreate();
	}

	public void open(String path, int flags, int mode) {
		DatabaseWrapper.envOpen(pointer, path, flags, mode);
	}

	public void close() {
		DatabaseWrapper.envClose(pointer);
	}

}
