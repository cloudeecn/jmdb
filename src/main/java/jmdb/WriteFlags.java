package jmdb;

public class WriteFlags {
	public static final int MDB_NOOVERWRITE = 0x10;
	public static final int MDB_NODUPDATA = 0x20;
	public static final int MDB_CURRENT = 0x40;
	public static final int MDB_RESERVE = 0x10000;
	public static final int MDB_APPEND = 0x20000;
	public static final int MDB_APPENDDUP = 0x40000;
	public static final int MDB_MULTIPLE = 0x80000;
}
