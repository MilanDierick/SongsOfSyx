package util.data;

import util.data.BOOLEAN_OBJECT.BOOLEAN_OBJECTE;
import util.data.INT_O.INT_OE;
import util.info.INFO;

public abstract class DataO<T> {

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
	
	public DataO(){
		
	}
	
	protected abstract int[] data(T t);
	
	public int intCount() {
		return intCount;
	}
	
	public INT_OE<T> create(int max){
		if (max <= 0b01) {
			return new DataBit();
		}else if(max <= 0b011) {
			return new DataCrumb();
		}else if (max <= 0b01111) {
			return new DataNibble();
		}else if(max <= 0b011111111) {
			return new DataByte();
		}else if(max <= 0x0FFFF) {
			return new DataShort();
		}else {
			return new DataInt();
		}
	}
	
	public class DataBit implements INT_OE<T>, BOOLEAN_OBJECTE<T>{
		
		private final int index;
		private final INFO info;
		
		public DataBit(INFO info) {
			if (bitCount >= 32) {
				
				bitInt = intCount;
				intCount ++;
				bitCount = 0;
			}
			
			index = (bitInt << 5) | bitCount;
			bitCount += 1;
			this.info = info;
		}
		
		public DataBit() {
			this(null);
		}
		
		public DataBit(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc));
		}

		@Override
		public boolean is(T t) {
			return get(t) == 1;
		}

		@Override
		public BOOLEAN_OBJECTE<T> set(T t, boolean b) {
			set(t, b ? 1 : 0);
			return this;
		}
		
		@Override
		public INFO info() {
			return info;
		}

		@Override
		public int get(T t) {
			return (data(t)[index>>5] >> (index & 0b011111)) & 0b01;
		}

		@Override
		public int min(T t) {
			return 0;
		}

		@Override
		public int max(T t) {
			return 1;
		}
		
		@Override
		public void set(T t, int s) {
			s &= 1;
			int scroll = index & 0b011111;
			int mask = 1 << scroll;
			int i = index>>5;
			data(t)[i] &= ~mask;
			s = s << scroll;
			data(t)[i] |= s;

		}
		
	}
	
	public class DataCrumb implements INT_OE<T>{
		
		private final int index;
		private final INFO info;
		
		public DataCrumb(INFO info) {
			if (crumbCount >= 32) {
				
				crumbInt = intCount;
				intCount ++;
				crumbCount = 0;
			}
			
			index = (crumbInt << 5) | crumbCount;
			crumbCount += 2;
			
			this.info = info;
		}
		
		public DataCrumb() {
			this(null);
		}
		
		public DataCrumb(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc));
		}
		
		@Override
		public int get(T t) {
			return (data(t)[index>>5] >> (index & 0b011111)) & 0b00011;
		}

		@Override
		public int min(T t) {
			return 0;
		}

		@Override
		public int max(T t) {
			return 3;
		}

		@Override
		public void set(T t, int s) {
			s &= 0x03;
			int scroll = index & 0b011111;
			int mask = 0x03 << scroll;
			int i = index>>5;
			data(t)[i] &= ~mask;
			s = s << scroll;
			data(t)[i] |= s;
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
	}
	
	public class DataNibble implements INT_OE<T>{
		
		private final int max;
		private final int index;
		private final INFO info;
		
		public DataNibble(int max, INFO info) {
			if (nibbleCount >= 32) {
				
				nibbleInt = intCount;
				intCount ++;
				nibbleCount = 0;
			}
			
			index = (nibbleInt << 5) | nibbleCount;
			nibbleCount += 4;
			this.info = info;
			this.max = max;
		}
		
		public DataNibble() {
			this(15, null);
		}
		
		public DataNibble(int max) {
			this(max, null);
		}
		
		public DataNibble(INFO info) {
			this(15, info);
		}
		
		public DataNibble(CharSequence name, CharSequence desc) {
			this(15, new INFO(name, desc));
		}
		
		@Override
		public int get(T t) {
			return (data(t)[index>>5] >> (index & 0b011111)) & 0x000F;
		}

		@Override
		public int min(T t) {
			return 0;
		}

		@Override
		public int max(T t) {
			return max;
		}

		@Override
		public void set(T t, int s) {
			s &= 0x0F;
			int scroll = index & 0b011111;
			int mask = 0x0F << scroll;
			int i = index>>5;
			data(t)[i] &= ~mask;
			s = s << scroll;
			data(t)[i] |= s;
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
	}
	
	public class DataNibble1 implements INT_OE<T>{

		private final DataBit bit;
		private final DataNibble nibble;
		private final int max;
		
		public DataNibble1() {
			this(0b011111);
		}
		
		public DataNibble1(int max) {
			bit = new DataBit();
			nibble = new DataNibble();
			this.max = max;
		}
		
		@Override
		public int get(T t) {
			return (bit.get(t)<<4)+nibble.get(t);
		}

		@Override
		public int min(T t) {
			return 0;
		}

		@Override
		public int max(T t) {
			return max;
		}

		@Override
		public void set(T t, int v) {
			int b = (v >> 4)&1;
			bit.set(t, b);
			nibble.set(t, (v&0xF));
		}
		
	}
	
	public class DataByte implements INT_OE<T>{
		
		private final int index;
		private final INFO info;
		private final int max;
		
		public DataByte(INFO info, int max){
			if (byteCount >= 32) {
				
				byteInt = intCount;
				intCount ++;
				byteCount = 0;
			}
			
			index = (byteInt << 5) | byteCount;
			byteCount += 8;
			this.info = info;
			this.max = max;
		}
		
		public DataByte(INFO info){
			this(info, 255);
		}
		
		public DataByte(int max){
			this(null, max);
		}
		
		public DataByte() {
			this(null);
		}
		
		public DataByte(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc));
		}
		
		@Override
		public int get(T t) {
			return (data(t)[index>>5] >> (index & 0b011111)) & 0x000FF;
		}

		@Override
		public int min(T t) {
			return 0;
		}

		@Override
		public int max(T t) {
			return max;
		}

		@Override
		public void set(T t, int s) {
			if (s < min(t) || s > max(t))
				throw new RuntimeException(""+s);
			int scroll = index & 0b011111;
			int mask = 0x0FF << scroll;
			int i = index>>5;
			data(t)[i] &= ~mask;
			
			s = s << scroll;
			data(t)[i] |= s;
			
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
	}
	
	public class DataShort implements INT_OE<T>{
	
		private final int index;
		private final INFO info;
		private final int max;
		
		public DataShort(INFO info, int max) {
			this.info = info;
			this.max = max;
			if (shortCount >= 32) {
				
				shortInt = intCount;
				intCount ++;
				shortCount = 0;
			}
			
			index = (shortInt << 5) | shortCount;
			shortCount += 16;
		}
		
		public DataShort(INFO info) {
			this(info, Short.MAX_VALUE);
		}
		
		public DataShort() {
			this(null);
		}
		
		public DataShort(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc));
		}
		
		public DataShort(CharSequence name, CharSequence desc, int max) {
			this(new INFO(name, desc), max);
		}
		
		@Override
		public int get(T t) {
			return (data(t)[index>>5] >> (index & 0b011111)) & 0x0FFFF;
		}

		@Override
		public int min(T t) {
			return 0;
		}

		@Override
		public int max(T t) {
			return max;
		}

		@Override
		public void set(T t, int s) {
			s &= 0x0FFFF;
			int scroll = index & 0b011111;
			int mask = 0x0FFFF << scroll;
			int i = index>>5;
			data(t)[i] &= ~mask;
			s = s << scroll;
			data(t)[i] |= s;
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
	}
	
	public class DataInt implements INT_OE<T>{
		
		private final int index;
		private final int max;
		private final INFO info;
		
		public DataInt(INFO info, int max) {
			this.info = info;
			intCount++;
			index = intCount-1;
			this.max = max;
		}
		
		public DataInt() {
			this(null, Integer.MAX_VALUE);
			
		}
		
		public DataInt(INFO info) {
			this(info, Integer.MAX_VALUE);
		}
		
		public DataInt(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc), Integer.MAX_VALUE);
		}
		
		@Override
		public int get(T t) {
			return data(t)[index];
		}

		@Override
		public int min(T t) {
			return 0;
		}

		@Override
		public int max(T t) {
			return max;
		}

		@Override
		public void set(T t, int i) {
			data(t)[index] = i;
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
	}
	
}
