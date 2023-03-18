package snake2d;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.lwjgl.system.MemoryUtil;

public class VboTest {

	private final static int size = 10000000;
	private final static int testsAmount = 40;
	
	private final static Test vanilla = new Test("Put Byte") {
		
		@Override
		void test(ByteBuffer b) {
			for (int i = 0; i < size; i++)
				b.put((byte)i);
		}
		
	};
	
	private final static Test bulk = new Test("Put Byte Bulk") {

		private final byte[] bytes = new byte[size];
		
		@Override
		void test(ByteBuffer b) {
			for (int i = 0; i < size; i++)
				bytes[i] = (byte) i;
			b.put(bytes);
		}
		
	};
	
	private final static Test bulkShort = new Test("Put Byte Bulk Short") {

		private final byte[] bytes = new byte[size];
		
		@Override
		void test(ByteBuffer b) {
			for (int i = 0; i < size; i+=2) {
				bytes[i] = b1(i);
				bytes[i+1] = b2(i);
			}
			b.put(bytes);
		}
		
		private final byte b1(int x) {
			return (byte) (x & 0x0FF);
		}
		
		private final byte b2(int x) {
			return (byte) ((x>>8) & 0x0FF);
		}
		
	};
	
	private final static Test bulkShort2 = new Test("Put Byte Bulk Short2") {

		private final short[] bytes = new short[size/2];
		
		@Override
		void test(ByteBuffer b) {
			for (int i = 0; i < size/2; i++) {
				bytes[i] = (short) i;
			}
			ShortBuffer sBuff = b.asShortBuffer();
			sBuff.put(bytes);
		}
		
//		private final byte b1(int x) {
//			return (byte) (x & 0x0FF);
//		}
//		
//		private final byte b2(int x) {
//			return (byte) ((x>>8) & 0x0FF);
//		}
		
	};
	
	private final static Test[] tests = new Test[] {vanilla, bulk, bulkShort, bulkShort2};
	
	public static void main(String[] args) {
		
		final Test[] testA = tests();
		
		ByteBuffer b = MemoryUtil.memAlloc(size); //ByteBuffer.allocate(size);
		
		for (Test test : testA) {
			long t = System.currentTimeMillis();
			test.test(b);
			t = System.currentTimeMillis()-t;
			test.time += t;
			b.clear();
		}
		
		for (Test t : tests) {
			Printer.ln(t.name + " " + t.time);
		}
	}
	
	private static Test[] tests() {
		Test[] res = new Test[tests.length*testsAmount];
		
		int i = 0;
		for (Test t : tests) {
			for (int k = 0; k < testsAmount; k++)
				res[i++] = t;
		}
		
		Random r = ThreadLocalRandom.current();
		
		for (int k = 0; k < res.length*2; k++) {
			int a = r.nextInt(res.length);
			int b = r.nextInt(res.length);
			Test old = res[a];
			res[a] = res[b];
			res[b] = old;
		}
		return res;
		
	}
	
	private static abstract class Test {

		long time;
		final String name;
		
		Test(String name){
			this.name = name;
		}
		
		abstract void test(ByteBuffer b);
		
	}
	
}
