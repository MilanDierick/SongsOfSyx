package snake2d;

import snake2d.Path.COST;
import snake2d.Path.DEST;

public class PathAsyncOrder {

	private volatile short startX;
	private volatile short startY;
	private final SHORTEST shortest = new SHORTEST(); 
	private volatile DEST destMethod;
	private volatile COST costMethod;
	private volatile Path.Async path;
	private volatile double maxValue;
	
	final double getCost(int fromX, int fromY, int toX, int toY){
		return costMethod.getCost(fromX, fromY, toX, toY); 
	}
	
	final float getOptDistance(int x, int y){
		return destMethod.getOptDistance(x, y);
	}
	
	final boolean isDest(int x, int y){
		return destMethod.isDest(x, y);
	}
	
	final double maxValue() {
		return maxValue;
	}
	
	final void setShortest(Path.Async p, COST method, double maxValue, int aX, int aY, int bX, int bY){
		this.costMethod = method;
		startX = (short) aX;
		startY = (short) aY;
		shortest.set(bX, bY);
		this.destMethod = shortest;
		this.path = p;
		this.maxValue = maxValue;
	}
	
	final void setNearest(Path.Async p, COST method, DEST dMethod, double maxValue, int startX, int startY){
		this.costMethod = method;
		this.destMethod = dMethod;
		this.startX = (short) startX;
		this.startY = (short) startY;
		this.maxValue = maxValue;
		this.path = p;
	}
	
	final int getStartX(){
		return startX;
	}
	
	final int getStartY(){
		return startY;
	}
	
	final Path.Async getPath(){
		return path;
	}
	
	private final static class SHORTEST extends DEST{

		private volatile int destX,destY;
		
		final void set(int destX, int destY){
			this.destX = destX;
			this.destY = destY;
		}
		
		@Override
		protected final float getOptDistance(int x, int y) {
			//Manhattan distance
			return Math.abs(destX - x) + Math.abs(destY - y);
		}

		@Override
		protected final boolean isDest(int x, int y) {
			return x == destX && y == destY;
		}
	}
	
}
