package jmdb;

import java.nio.ByteBuffer;

public class EnvStat {

	ByteBuffer pointer;

	public EnvStat() {
		pointer = ByteBuffer.allocateDirect(DatabaseWrapper.getStatSize());
	}

}
