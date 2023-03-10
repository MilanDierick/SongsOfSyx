package snake2d.util.sets;

import java.io.IOException;

import snake2d.util.file.*;

/**
 * dear mortal. This is not your ordinary bitmap. This is a bit(s)map. That's right.
 * How many bits you want? Up to you. Took me two days to get this right...
 * @author mail__000
 *
 */
public class Bitsmap1D implements SAVABLE{
	
	private final int[][] bits;
	private final int outof;
	private final int max;
	
	/**
	 * 
	 * @param outof - value to return if index is out of bounds
	 * @param bits - the number of bits you want to store a value. Will be positive. Should be < 32 
	 * @param amount - how many of these memory chunks do you want, huh?
	 */
	public Bitsmap1D(int outof, int bits, int amount){
		this.bits = new int[bits][amount/32 + (amount%32 > 0 ? 1 : 0)];
		this.outof = outof;
		max = amount;
	}
	
	@Override
	public final void save(FilePutter fp) {
		fp.is(bits);
	}
	
	@Override
	public final void load(FileGetter fp) throws IOException {
		fp.readArray(bits);
	}
	
	/**
	 * 
	 * @param index - your index
	 * @return - your value, or outof value if out of bounds
	 */
	public int get(int index){
		
		if (index < 0 || index >= max)
			return outof;
		
		int res = 0;
		int s = index & 0b011111;
		int k = index >> 5;
		
		for (int i = 0; i < bits.length; i++) {
			int v = bits[i][k];
			v = v >> s;
			v &= 0b01;
			res |= v << i;
		}
		
		return res;
		
	}

	@Override
	public void clear() {
		for (int i = 0; i < bits.length; i++) {
			for (int k = 0; k < bits[0].length; k++)
				bits[i][k] = 0;
		}
	}
	
	/**
	 * 
	 * @param index
	 * @param value
	 */
	public void set(int index, int value){
		
		int scroll = index & 0b011111;
		index = index >> 5;
		
		for (int i = 0; i < bits.length; i++) {
			bits[i][index] &= ~(1 << scroll);
			bits[i][index] |= (value & 0b01) << scroll;
			value = value >> 1;
		}
		
	}
	
	public void setAll(int value) {
		
		for (int y = 0; y < bits.length; y++) {
			int i = ((value >>> y) & 1);
			i = (i == 1) ? -1 : 0;
			for (int x = 0; x < bits[0].length; x++) {
				bits[y][x] = i;
			}
		}
	}
	
	
	public int maxIndex() {
		return max;
	}
	
	public int maxValue() {
		return (1 << bits.length)-1;
	}
	

	
}
