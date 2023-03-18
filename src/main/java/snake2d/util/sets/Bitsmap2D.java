package snake2d.util.sets;


public class Bitsmap2D {
	
	private final Bitsmap1D map;
	private final int width;
	
	public Bitsmap2D(int outof, int bits, int width, int height){
		map = new Bitsmap1D(outof, bits, width*height);
		this.width = width;
	}
	
	public int get(int x, int y){
		int i = x + y*width;
		return map.get(i);
	}

	public void set(int x, int y, int value){
		int i = x + y*width;
		map.set(i, value);
	}
	
}
