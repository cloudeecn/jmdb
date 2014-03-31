package jmdb;

import java.nio.ByteBuffer;

public class EnvInfo {
	ByteBuffer pointer;

	public EnvInfo() {
		pointer = ByteBuffer.allocateDirect(DatabaseWrapper.getEnvInfoSize());
	}
}
