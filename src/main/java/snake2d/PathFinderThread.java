package snake2d;

import snake2d.Path.COST;
import snake2d.Path.DEST;
import snake2d.util.datatypes.DIR;



/**
 * You can have one or many of these. They cost memory though. 50-100 bytes per tile.
 * @author mail__000
 *
 */
class PathFinderThread extends Thread{

	private static volatile int id = 0;
	final static int MAX_LOAD = 100;
	private final PathAsyncOrder[] orders = new PathAsyncOrder[MAX_LOAD];
	private int lastOrder = 0;
	private int currentOrder = 0;
	private volatile boolean lock = false;
	
	private final PathTile[][] tiles;
	private int iID = 0;
	private final RBTileTree unprocessed;
	private PathTile dest;
	private volatile boolean running = true;
	
	PathFinderThread(PathThreadManager m, int tileSizeX, int tilesizeY){
		if (tileSizeX > Short.MAX_VALUE || tilesizeY > Short.MAX_VALUE)
			throw new RuntimeException("too big size for pathfinder");
		
		unprocessed = new RBTileTree();
		
		for (int i = 0; i < MAX_LOAD; i++)
			orders[i] = new PathAsyncOrder();
		
		tiles = new PathTile[tileSizeX][tilesizeY];
		for (int y = 0; y < tilesizeY; y++)
			for (int x = 0; x < tileSizeX; x++)
				tiles[y][x] = new PathTile((short)x, (short)y);
	}
	
	int getLoad(){
		if (lastOrder > currentOrder){
			return lastOrder - currentOrder;
		}else if(lastOrder < currentOrder){
			return MAX_LOAD - currentOrder + lastOrder;
		}else
			return 0;
	}
	
	boolean addNearest(Path.Async p, COST method, DEST dMethod, double max, int startX, int startY){
		takeLock();
		if (getLoad() < MAX_LOAD-1){
			PathAsyncOrder order = orders[lastOrder];
			lastOrder = (lastOrder + 1)%MAX_LOAD;
			lock = false;
			order.setNearest(p, method, dMethod, max, startX, startY);
			if (!this.isInterrupted())
				this.interrupt();
			return true;
		}
		lock = false;
		return false;
	}
	
	boolean addShortest(Path.Async p, COST method, double max, int aX, int aY, int bX, int bY){
		takeLock();
		if (getLoad() < MAX_LOAD-1){
			PathAsyncOrder order = orders[lastOrder];
			lastOrder = (lastOrder + 1)%MAX_LOAD;
			order.setShortest(p, method, max, aX, aY, bX, bY);
			lock = false;
			if (!this.isInterrupted())
				this.interrupt();
			return true;
		}
		lock = false;
		return false;
	}
	
	private void takeLock(){
		while(lock);
		lock = true;
	}
	
	@Override
	public void run(){
		Thread.currentThread().setName("pathfinder#:" + id++);
		while (running){
			takeLock();
			if (currentOrder != lastOrder){
				PathAsyncOrder o = orders[currentOrder];
				currentOrder = (currentOrder + 1)%MAX_LOAD;
				lock = false;
				find(o);
			}else{
				lock = false;
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					continue;
				}
			}
		}
		
	}
	
	private void find(PathAsyncOrder order){
		
		iID ++;
		if (iID == 0){
			for (int y = 0; y < tiles.length; y++)
				for (int x = 0; x < tiles[0].length; x++)
					tiles[y][x].pathId = 0;
			iID = 1;
		}
		
		unprocessed.clear();
		add2OpenSet(tiles[order.getStartY()][order.getStartX()], null, 0, order);

		while(unprocessed.size() > 0){
			if (process(unprocessed.pollSmallest(), order)){
				order.getPath().set(dest);
				order.getPath().unlock();
				order.getPath().pathCalculated(true);
				return;
			}
		}
		
		order.getPath().cancel();
		order.getPath().unlock();
		order.getPath().pathCalculated(false);
	
	}
	
	private void add2OpenSet(PathTile t, PathTile parent, double accCost, PathAsyncOrder order){
		t.accCost = (float) accCost;
		t.value = (float) (accCost + order.getOptDistance(t.x, t.y));
		tiles[t.y][t.x].pathId = iID;
		tiles[t.y][t.x].closed = false;
		t.pathParent = parent;
		unprocessed.put(t);
	}
	
	private boolean process(PathTile t, PathAsyncOrder order){
		
		int x = t.x;
		int y = t.y;
		close(x, y);
		
		for (int i = 0; i < DIR.ALL.size(); i++){
			DIR d = DIR.ALL.get(i);
			int ytemp = y + d.y();
			int xtemp = x + d.x();
			
			if (order.isDest(xtemp, ytemp)){
				dest = t;
				return true;
			}
			
			if (isClosed(xtemp, ytemp)) {
				continue;
			}
			double tempCost = order.getCost(x, y, xtemp, ytemp);
			if (tempCost < 0){
				if (tempCost == -1){
					close(xtemp,ytemp);
				}
				continue;
			}
			tempCost*= d.tileDistance();
			tempCost += t.accCost;
			
			dest = tiles[ytemp][xtemp];
			
			
			if (dest.pathId == iID){
				
				if (dest.accCost > tempCost){
					unprocessed.remove(dest);
					add2OpenSet(dest, t, tempCost, order);
				}else{
					dest.closed = true;
				}
			}else{
				add2OpenSet(dest, t, tempCost, order);

			}
		}
		return false;
		
	}
	
	private boolean isInserted(int x, int y){
		return tiles[y][x].pathId == iID;
	}
	
	private boolean isClosed(int x, int y){
		return (x < 0 || y < 0 || x >= tiles[0].length || y >= tiles.length || (isInserted(x,y) && tiles[y][x].closed));
	}
	
	private void close(int x, int y){
		tiles[y][x].closed = true; 
	}
	
	void kill(){
		running = false;
		if (!this.isInterrupted())
			this.interrupt();
	}
}
