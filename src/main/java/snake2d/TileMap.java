package snake2d;

import snake2d.util.sets.Bitmap1D;

class TileMap {

	private final float[] accCosts;
	private final float[] values;
	private final int[] parents;
	private final int[] ids;
	private final Bitmap1D closed;
	private final int[] left;
	private final int[] right;
	private final int[] parent;
	private final Bitmap1D color;
	private final short[] x;
	private final short[] y;
	
	TileMap(int width, int height){
		
		int size = width*height;
		accCosts = new float[size];
		values = new float[size];
		parents = new int[size];
		ids = new int[size];
		closed = new Bitmap1D(size, false);
		left = new int[size];
		right = new int[size];
		parent = new int[size];
		color = new Bitmap1D(size, false);
		x = new short[size];
		y = new short[size];
		int i = 0;
		for (int yi = 0; yi < height; yi++){
			for (int xi = 0; xi < width; xi++){
				x[i] = (short) xi;
				y[i] = (short) yi;
				i++;
			}
		}
	}
	
	float getAccCost(int i){
		return accCosts[i];
	}
	
	void setAccCost(int i, float cost){
		accCosts[i] = cost;
	}
	
	float getValue(int i){
		return values[i];
	}
	
	void setValue(int i, float value){
		values[i] = value;
	}
	
	int getParent(int i){
		return parents[i];
	}
	
	void setParent(int i, int parentI){
		parents[i] = parentI;
	}
	
	int getPathId(int i){
		return ids[i];
	}
	
	int x(int i){
		return x[i];
	}
	
	int y(int i){
		return y[i];
	}
	
	void setPathID(int i, int id){
		ids[i] = id;
	}
	
	boolean isClosed(int i){
		return closed.get(i);
	}
	
	void setClosed(int i){
		closed.setTrue(i);
	}
	
	void setOpen(int i){
		closed.setTrue(i);
	}
	
	int left(int i){
		if (i == -1)
			return -1;
		return left[i];
	}
	
	void left(int i, int l){
		left[i] = l;
	}
	
	int right(int i){
		if (i == -1)
			return -1;
		return right[i];
	}
	
	void right(int i, int r){
		right[i] = r;
	}
	
	int parent(int i){
		if (i == -1)
			return -1;
		return parent[i];
	}
	
	void parent(int i, int l){
		parent[i] = l;
	}
	
	boolean color(int i){
		return color.get(i);
	}
	
	void color(int i, boolean yes){
		if (!yes)
			color.setFalse(i);
		else
			color.setTrue(i);
	}
	
	int compare(int a, int b){
		if (a == b)
			return 0;
		return getValue(a) < getValue(b) ? -1 : 1;
	}
	
}
