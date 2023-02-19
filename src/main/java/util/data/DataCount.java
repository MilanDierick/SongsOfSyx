package util.data;

public final class DataCount {

	private int intCount;
	private int shortCount = 32;
	private int shortInt;
	private int byteCount = 32;
	private int byteInt;
	private int nibbleCount = 32;
	private int nibbleInt;
	private int crumbCount = 32;
	private int crumbInt;
	private int bitCount = 32;
	private int bitInt;

	
	public DataCount(){
		
	}
	
	public int intCount() {
		return intCount;
	}
	
	public int nextInt() {
		
		intCount++;
		
		return intCount-1;
	}
	
	public int nextShort() {
		if (shortCount >= 32) {
			
			shortInt = intCount;
			intCount ++;
			shortCount = 0;
		}
		
		int res = (shortInt << 5) | shortCount;
		shortCount += 16;
		return res;
	}
	
	public static int getInt(int[] is, int index) {
		return is[index];
	}
	
	public static void setInt(int[] is, int index, int s) {
		is[index] = s;
	}
	
	public static int getShort(int[] is, int index) {
		return (is[index>>5] >> (index & 0b011111)) & 0x0FFFF;
	}
	
	public static void setShort(int[] is, int index, int s) {
		s &= 0x0FFFF;
		int scroll = index & 0b011111;
		int mask = 0x0FFFF << scroll;
		int i = index>>5;
		is[i] &= ~mask;
		s = s << scroll;
		is[i] |= s;
	}
	
	public int nextByte() {
		if (byteCount >= 32) {
			
			byteInt = intCount;
			intCount ++;
			byteCount = 0;
		}
		
		int res = (byteInt << 5) | byteCount;
		byteCount += 8;
		
		return res;
	}
	
	public static int getByte(int[] is, int index) {
		return (is[index>>5] >> (index & 0b011111)) & 0x000FF;
	}
	
	public static void setByte(int[] is, int index, int s) {
		s &= 0x0FF;
		int scroll = index & 0b011111;
		int mask = 0x0FF << scroll;
		int i = index>>5;
		is[i] &= ~mask;
		
		s = s << scroll;
		is[i] |= s;
	}
	
	public int nextNibble() {
		if (nibbleCount >= 32) {
			
			nibbleInt = intCount;
			intCount ++;
			nibbleCount = 0;
		}
		
		int res = (nibbleInt << 5) | nibbleCount;
		nibbleCount += 4;
		
		return res;
	}
	
	public static int getNibble(int[] is, int index) {
		return (is[index>>5] >> (index & 0b011111)) & 0x000F;
	}
	
	public static void setNibble(int[] is, int index, int s) {
		s &= 0x0F;
		int scroll = index & 0b011111;
		int mask = 0x0F << scroll;
		int i = index>>5;
		is[i] &= ~mask;
		s = s << scroll;
		is[i] |= s;
	}
	
	public static int getCrumb(int[] is, int index) {
		return (is[index>>5] >> (index & 0b011111)) & 0b00011;
	}
	
	public static void setCrumb(int[] is, int index, int s) {
		s &= 0x03;
		int scroll = index & 0b011111;
		int mask = 0x03 << scroll;
		int i = index>>5;
		is[i] &= ~mask;
		s = s << scroll;
		is[i] |= s;
	}
	
	public int nextCrumb() {
		if (crumbCount >= 32) {
			
			crumbInt = intCount;
			intCount ++;
			crumbCount = 0;
		}
		
		int res = (crumbInt << 5) | crumbCount;
		crumbCount += 2;
		
		return res;
	}
	
	public int nextBit() {
		if (bitCount >= 32) {
			
			bitInt = intCount;
			intCount ++;
			bitCount = 0;
		}
		
		int res = (bitInt << 5) | bitCount;
		bitCount += 1;
		return res;
	}
	
	public static int getBit(int[] is, int index) {
		return (is[index>>5] >> (index & 0b011111)) & 0b01;
	}
	
	public static void setBit(int[] is, int index, int s) {
		s &= 1;
		int scroll = index & 0b011111;
		int mask = 1 << scroll;
		int i = index>>5;
		is[i] &= ~mask;
		s = s << scroll;
		is[i] |= s;
	}
	
}
