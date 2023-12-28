package snake2d.util.map;

import snake2d.util.datatypes.*;

public interface MAP_BOOLEAN{

	public boolean is(int tile);
	
	public boolean is(int tx, int ty);
	
	public default boolean is(int tx, int ty, DIR d) {
		return is(tx+d.x(), ty+d.y());
	}
	
	public default boolean is(COORDINATE c) {
		return is(c.x(), c.y());
	}
	
	public default boolean is(COORDINATE c, DIR d) {
		return is(c.x()+d.x(), c.y()+d.y());
	}
	
	public static abstract class BooleanMap implements MAP_BOOLEAN{

		public final int width;
		public final RECTANGLE body;
		
		public BooleanMap(int width, int height) {
			this.width = width;
			this.body = new Rec(width, height);
		}
		
		public BooleanMap(DIMENSION dim) {
			this.width = dim.width();
			this.body = new Rec(dim.width(), dim.height());
		}

		@Override
		public boolean is(int tx, int ty) {
			return body.holdsPoint(tx, ty) && is(tx+ty*width);
		}

		
	}
	
}
