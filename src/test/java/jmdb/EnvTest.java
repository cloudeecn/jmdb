package jmdb;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@org.junit.FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EnvTest {

	private static final ThreadLocal<Env> env = new ThreadLocal<Env>();

	static {
		try {
			File file = new File("libjmdb.so");
			System.load(file.getCanonicalPath());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public void prepare() throws Exception {
		String path = "test";
		File dir = new File(path);
		dir.mkdirs();
		for (File file : dir.listFiles()) {
			file.delete();
		}
		Env env = new Env();
		try {
			env.setMapSize(16777216);
			env.setMaxDbs(16);
			env.setMaxReaders(16);
			env.open(path, 0, 384/* 600 */);
			EnvTest.env.set(env);
		} catch (Exception e) {
			try {
				env.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void test001Bootstrap() throws Exception {

	}

	private ThreadLocal<byte[]> keyBuffer = new ThreadLocal<byte[]>() {

		@Override
		protected byte[] initialValue() {
			return new byte[65536];
		}

	};

	private ThreadLocal<byte[]> valueBuffer = new ThreadLocal<byte[]>() {

		@Override
		protected byte[] initialValue() {
			return new byte[65536];
		}

	};

	private ThreadLocal<Random> rand = new ThreadLocal<Random>() {

		@Override
		protected Random initialValue() {
			return new Random();
		}
	};

	private String table = "!@#$%^&*()_+1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./<>?:啊哦呃";

	private void put(DB db, String key, String value) throws Exception {
		byte[] keyB = keyBuffer.get();
		int kofs = rand.get().nextInt(16);
		byte[] k = key.getBytes("utf-8");
		int klen = k.length;
		System.arraycopy(k, 0, keyB, kofs, klen);

		byte[] valueB = valueBuffer.get();
		int vofs = rand.get().nextInt(16);
		byte[] v = value.getBytes("utf-8");
		int vlen = v.length;
		System.arraycopy(v, 0, valueB, vofs, vlen);
		db.put(keyB, kofs, klen, valueB, vofs, vlen, 0);
	}

	private String get(DB db, String key) throws Exception {
		byte[] keyB = keyBuffer.get();
		int kofs = rand.get().nextInt(16);
		byte[] k = key.getBytes("utf-8");
		int klen = k.length;
		System.arraycopy(k, 0, keyB, kofs, klen);

		byte[] valueB = valueBuffer.get();
		int vofs = rand.get().nextInt(16);
		int vlen = db.get(keyB, kofs, klen, valueB, vofs, valueB.length - vofs);
		return new String(valueB, vofs, vlen, "utf-8");
	}

	private String randomString(int size) {
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < size; i++) {
			sb.append(table.charAt(rand.get().nextInt(table.length())));
		}
		return sb.toString();
	}

	@Test
	public void test002Simple() throws Exception {
		Env env = EnvTest.env.get();

		Transaction transaction = env.beginTransaction(false);
		try {
			TreeMap<String, String> ref = new TreeMap<String, String>();
			DB db = transaction.openDB("test", DBFlags.MDB_CREATE);
			for (int i = 0; i < 16384; i++) {
				String key = randomString(rand.get().nextInt(4) + 8);
				String value = randomString(rand.get().nextInt(4) + 8);
				ref.put(key, value);
				put(db, key, value);
			}
			transaction.commit();
			transaction = env.beginTransaction(true);
			db = transaction.openDB("test", DBFlags.MDB_CREATE);
			for (String refKey : ref.keySet()) {
				String refValue = ref.get(refKey);
				String value = get(db, refKey);
				Assert.assertEquals(refValue, value);
			}
			transaction.commit();
			transaction = env.beginTransaction(true);
			db = transaction.openDB("test", DBFlags.MDB_CREATE);
			for (String refKey : ref.keySet()) {
				String refValue = ref.get(refKey);
				String value = get(db, refKey);
				Assert.assertEquals(refValue, value);
			}
			transaction.rollback();
			transaction = env.beginTransaction(true);
			db = transaction.openDB("test", DBFlags.MDB_CREATE);
			String mid = null;
			Cursor cursor = db.openCursor();
			try {
				byte[] kbuf = keyBuffer.get();
				byte[] vbuf = valueBuffer.get();
				long result;
				boolean first = true;
				Iterator<Entry<String, String>> refEntryI = ref.entrySet()
						.iterator();
				int count = 0;
				while ((result = cursor.get(kbuf, 0, kbuf.length, vbuf, 0,
						vbuf.length, first ? CursorOp.MDB_FIRST
								: CursorOp.MDB_NEXT)) != -1) {
					if (!refEntryI.hasNext()) {
						Assert.assertTrue(false);
					}
					Entry<String, String> entry = refEntryI.next();
					first = false;
					int klen = (int) (result >>> 32);
					int vlen = (int) result;
					String key = new String(kbuf, 0, klen, "utf-8");
					String value = new String(vbuf, 0, vlen, "utf-8");
					Assert.assertEquals("Key mismatch at " + count,
							entry.getKey(), key);
					Assert.assertEquals("Key mismatch at " + count,
							entry.getValue(), value);
					count++;
					if (count == 8000) {
						mid = key;
					}
				}
			} finally {
				cursor.close();
			}
			cursor = db.openCursor();
			try {
				byte[] tmp = mid.getBytes("utf-8");
				int klen2 = tmp.length;
				byte[] kbuf = keyBuffer.get();
				byte[] vbuf = valueBuffer.get();
				System.arraycopy(tmp, 0, kbuf, 0, klen2);

				long result;
				boolean first = true;
				Iterator<Entry<String, String>> refEntryI = ref.tailMap(mid)
						.entrySet().iterator();
				int count = 0;
				while ((result = cursor.get(kbuf, 0,
						first ? klen2 : kbuf.length, vbuf, 0, vbuf.length,
						first ? CursorOp.MDB_SET : CursorOp.MDB_NEXT)) != -1) {
					if (!refEntryI.hasNext()) {
						Assert.assertTrue(false);
					}
					Entry<String, String> entry = refEntryI.next();
					first = false;
					int klen = (int) (result >>> 32);
					int vlen = (int) result;
					String key = new String(kbuf, 0, klen, "utf-8");
					String value = new String(vbuf, 0, vlen, "utf-8");
					Assert.assertEquals("Key mismatch at " + count,
							entry.getKey(), key);
					Assert.assertEquals("Key mismatch at " + count,
							entry.getValue(), value);
					count++;
					if (count == 8000) {
						mid = key;
					}
				}
			} finally {
				cursor.close();
			}
			transaction.rollback();
		} catch (Exception e) {
			transaction.rollback();
			throw e;
		}
	}

	@After
	public void finish() throws Exception {
		Env env = EnvTest.env.get();
		if (env != null) {
			EnvTest.env.remove();
			env.close();
			File dir = new File(env.getPath());
			for (File file : dir.listFiles()) {
				file.delete();
			}
		}
	}
}
