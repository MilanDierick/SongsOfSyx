package snake2d.util.sets;
import static snake2d.util.sets.Bitmap1D.*;

public class Bitmap2D {
	
	private final int[] bits;
	private final int max; 
	private final boolean outof;
	private final int width;
	
	public Bitmap2D(int width, int height, boolean outof){
		this.width = width;
		max = width*height;
		int l = max/32;
		if (l % 32 != 0)
			l++;
		bits = new int[l];
		this.outof = outof;
	}
	
	public boolean get(int x, int y){
		
		int bit = x + y*width;
		
		if (bit < 0 || bit > max)
			return outof;
		
		int m = bit & 0x0000001F;
		int i = bit >> 5;
		
		return (bits[i] & masks[m]) == masks[m];
		
	}
	
	public void setTrue(int x, int y){
		int bit = x + y*width;
		if (bit < 0 || bit > max)
			return; 
		
		int m = bit & 0x0000001F;
		int i = bit >> 5;
		bits[i] |= masks[m];
		
	}
	
	public void setFalse(int x, int y){
		int bit = x + y*width;
		if (bit < 0 || bit > max)
			return; 
		
		int m = bit & 0x0000001F;
		int i = bit >> 5;
		bits[i] &= imasks[m];
		
	}

	
}
