package snake2d;

import snake2d.Path.COST;
import snake2d.Path.DEST;


/**
 * A multi-threaded A star pathfinder with weights. It is a piece of art.
 * @author mail__000
 *
 */
public class PathThreadManager extends CORE_RESOURCE{

	private final PathFinderThread[] finders;
	private int id = 1;
	private final int maxX, maxY;
	
	/**
	 * 
	 * @param threads how many threads?
	 * @param maxX Max pathlength x-axis;
	 * @param maxY Max pathlength y-axis
	 */
	public PathThreadManager(int threads, int sizeX, int sizeY){
		
		maxX = sizeX;
		maxY = sizeY;
		
		if (threads < 1)
			threads = 1;
		else if (threads > 5)
			threads = 5;
		
		finders = new PathFinderThread[threads];

		
		for (int i = 0; i < finders.length; i++){
			finders[i] = new PathFinderThread(this, sizeX, sizeY);
			finders[i].setDaemon(true);
			finders[i].setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					e.printStackTrace();
					CORE.annihilate(new RuntimeException(e));
				}
			});
			finders[i].start();
		}
		CORE.addDisposable(this);
		
	}
	
	/**
	 * Get a path from a to b
	 * @param client
	 * @param method
	 * @param aX
	 * @param aY
	 * @param bX
	 * @param bY
	 * @return
	 * 0 if you're already there
	 * 1-max int if the request is going to be processed
	 * -1 if pathmanager is too busy
	 * 	 * -2 if you're a retard and the path is undergoing processing
	 */
	public int getShortest(Path.Async p, COST method, int aX, int aY, int bX, int bY){
		return getShortest(p, method, Double.MAX_VALUE, aX, aY, bX, bY);
	}
	
	public int getShortest(Path.Async p, COST method, double maxCost, int aX, int aY, int bX, int bY) {
		if (p.isBusy())
			return -2;
		
		id++;
		if (id < 0)
			id = 1;
		
		if (aX == bX && aY == bY)
			return 0;
		if (Math.abs(aX-bX) <= 1 && Math.abs(aY-bY) <= 1)
			return 0;
		
		
		if (aX < 0 || aX > maxX || aY < 0 || aY > maxY || bX < 0 || bX > maxX || bY < 0 || bY > maxY){
			return 0;
		}
		
		PathFinderThread selected = getFreeThread();
		
		if (selected != null){
			p.lock();
			selected.addShortest(p, method, maxCost, aX, aY, bX, bY);
			
			return id;
		}
		p.cancel();
		return -1;
	}

	/**
	 * Get the path to the closest tile that the method will return 0 on.
	 * @param client
	 * @param method
	 * @param startX
	 * @param startY
	 * @return
	 * 0 if you're already there
	 * 1-max int if the request is going to be processed
	 * -1 if pathmanager is too busy
	 * -2 if you're a retard and the path is undergoing processing
	 */
	public int getNearest(Path.Async p, COST method, DEST dMethod, int startX, int startY){
		
		return getNearest(p, method, dMethod, Double.MAX_VALUE, startX, startY);
		
	}
	
	public int getNearest(Path.Async p, COST method, DEST dMethod, double maxValue, int startX, int startY){
		
		if (p.isBusy())
			return -2;
		
		id++;
		if (id < 0)
			id = 1;

		if (dMethod.isDest(startX, startY))
			return 0;
		
		PathFinderThread selected = getFreeThread();
		if (selected != null){
			p.lock();
			selected.addNearest(p, method, dMethod, maxValue, startX, startY);
			
			return id;
		}
		p.cancel();
		return -1;
		
	}
	
	private PathFinderThread getFreeThread(){

		int i = PathFinderThread.MAX_LOAD-1;
		PathFinderThread selected = null;
		
		for (int j = 0; j < finders.length; j++){
			if (finders[j].getLoad() < i){
				i = finders[j].getLoad();
				selected = finders[j];
			}
		}
		return selected;
	}
	
	public boolean isMakingPaths(){
		for (PathFinderThread p : finders){
			if (p.getLoad() > 0){
				return true;
			}
		}
		return false;
	}
	
	public int getLoadPercent(){
		
		double tot = 0;
		double l = 0;
		for (PathFinderThread p : finders){
			tot = PathFinderThread.MAX_LOAD;
			l += p.getLoad();
		}
		
		return (int) (l/tot);
		
	}
	
	@Override
	public void dis() {
		for (PathFinderThread p : finders){
			p.kill();
		}
		
	}
	
}
