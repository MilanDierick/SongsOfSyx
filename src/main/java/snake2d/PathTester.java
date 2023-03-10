package snake2d;

import java.util.Random;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

class PathTester{

	public static void main(String... args){
		
		new Flooder();
		//new Online();
		//new Creation();
		
	}
	
	private PathTester(){
		
	}

	
}

class Debug{
	
	private volatile int count;
	private final int amount = 9999;
	private Path.Async[] unused = new Path.Async[amount+1];
	private int size = 768;
	private int threads = 3;
	
	public Debug(){
		for (int i = 0; i < unused.length; i++){
			unused[i] = new Path.Async(500) {

				@Override
				protected void pathCalculated(boolean success) {
					count ++;
					
				}
			};
		}
		
		count = 0;
		Random r = new Random();
		
		PathThreadManager pm = new PathThreadManager(threads, size, size);
		
		final float[][] lookup = new float[size][size]; 
		
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++)
				lookup[y][x] = -1 + r.nextInt(4);
		
		
//		for (int y = 0; y < size; y++)
//			lookup[y][355] = -1;
		
		final Path.COST cm = new Path.COST() {
			@Override
			public double getCost(int fromX, int fromY, int toX, int toY) {
				
//				if (fromX != toX && fromY != toY){
//					return lookup[toY][toX] *1.5;
//				}
				
				//System.out.println(lookup[toY][toX] * fromX != toX && fromY != toY ? 1.5 : 1);
				return lookup[toY][toX];
			}
		};
		
		long now = System.currentTimeMillis();
		
		int a = amount;
		int i = 0;
		
		while (a >= 0){
			
			int sx = r.nextInt(size);
			int sy = r.nextInt(size);
			int dy = r.nextInt(size);
			int dx = r.nextInt(size);
			
//			System.out.println("PATH!");
//			System.out.println("from: x" + sx + " y" + sy);
//			System.out.println("to: x" + dx + " y" + dy);
			
			if (pm.getShortest(unused[i], cm, sx, sy, dx, dy) > 0){
				a--;
				i++;
				System.out.println(count);
				
			}
			
		}
		
		while (count < amount){
			System.out.println(count + " of " + amount);
		}
		System.out.println(count);
		System.out.println(System.currentTimeMillis() - now);
		long heapSize = Runtime.getRuntime().totalMemory(); 
		System.out.println("size: " + heapSize/1000000);
		pm.dis();
	}
}

class Creation{
	
	private volatile int count;
	private final int amount = 9999;
	private int size = 768;
	private int threads = 3;
	
	public Creation(){
		
		
		count = 0;
		Random r = new Random();
		
		PathThreadManager pm = new PathThreadManager(threads, size, size);
		
		final float[][] lookup = new float[size][size]; 
		
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++)
				lookup[y][x] = (r.nextInt(10) == 0) ? -1 : 1;
		
		final Path.COST cm = new Path.COST() {
			@Override
			public double getCost(int fromX, int fromY, int toX, int toY) {
				return lookup[toY][toX];
			}
		};
		
		long heap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		heap /= 1000000;
		System.out.println("---------------------");
		System.out.println("       CREATION      ");
		System.out.println("---------------------");
		System.out.println("starting processing of " + amount + " paths, heap at: " + heap + "mb");
		
		long now = System.currentTimeMillis();
		
		int a = amount;
		int i = 0;
		
		while (a >= 0){
			
			int sx = r.nextInt(size);
			int sy = r.nextInt(size);
			int dy = r.nextInt(size);
			int dx = r.nextInt(size);
			Path.Async p = new Path.Async(500) {

				@Override
				protected void pathCalculated(boolean success) {
					count++;
					
				}
			};
			if (pm.getShortest(p, cm, sx, sy, dx, dy) > 0){
				a--;
				i++;
				if (i > 1000){
					System.out.println("count: " + count + " ...");
					i = 0;
				}
			}

			
			
		}
		
		while (count < amount){
			
		}
		System.out.println("count: " + count);
		System.out.println("time: " + (System.currentTimeMillis() - now) + "ms");
		long heapSize = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		heapSize /= 1000000;
		System.out.println("heapsize: " + heapSize + "mb" + " (+" + (heapSize - heap) + ")");
		pm.dis();
	}
}

class Pooling{
	
	private volatile int count;
	private final int amount = 2500;
	private Path.Async[] unused = new Path.Async[amount+1];
	private int size = 768;
	private int threads = 3;
	private volatile int a;
	private volatile int i;
	
	public Pooling(){
		

		
		
		for (int i = 0; i < unused.length; i++){
			unused[i] = new Path.Async(500) {
				
				@Override
				protected void pathCalculated(boolean success) {
					if (success)
						count ++;
					
				}
			};
		}
		
		count = 0;
		Random r = new Random();
		
		PathThreadManager pm = new PathThreadManager(threads, size, size);
		
		final float[][] lookup = new float[size][size]; 
		
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++)
				lookup[y][x] = (r.nextInt(10) == 0) ? -1 : 1;
		
		final Path.COST cm = new Path.COST() {
			@Override
			public double getCost(int fromX, int fromY, int toX, int toY) {
				return lookup[toY][toX];
			}
		};
		
		long heap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		heap /= 1000000;
		System.out.println("---------------------");
		System.out.println("       POOLING       ");
		System.out.println("---------------------");
		System.out.println("starting processing of " + amount + " paths, heap at: " + heap + "mb");
		
		
		long now = System.currentTimeMillis();
		
		a = amount;
		i = 0;
		
		while (a >= 0){
			
			int sx = r.nextInt(size);
			int sy = r.nextInt(size);
			int dy = r.nextInt(size);
			int dx = r.nextInt(size);

			if (pm.getShortest(unused[i], cm, sx, sy, dx, dy) > 0){
				a--;
				i++;
				if ((i & 255) == 0){
					System.out.println("count: " + count + " ...");
				}
				
			}
			
		}
		
		while (count <= amount){
			
		}
		System.out.println("count: " + count);
		System.out.println("time: " + (System.currentTimeMillis() - now) + "ms");
		long heapSize = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		heapSize /= 1000000;
		System.out.println("heapsize: " + heapSize + "mb" + " (+" + (heapSize - heap) + ")");
		
		pm.dis();
	}
}

class Flooder{
	
	private int amount = 100;
	private int size = 768;

	Flooder(){
		
		Random r = new Random();


		System.out.println("---------------------");
		System.out.println("       Flooder       ");
		System.out.println("---------------------");
		System.out.println("settings " + amount + " size: " + size);
		
		System.out.println("Flooder");
		
		
		PathUtilOnline paths = new PathUtilOnline(size);
		
		long now = System.currentTimeMillis();
		long pushes = 0;
		
		for (int i = 0; i < amount; i++) {
			int sx = r.nextInt(size);
			int sy = r.nextInt(size);
			PathUtilOnline.Flooder f = paths.getFlooder();
			f.init(this);
			f.pushSloppy(sx, sy, 0);
			
			while(f.hasMore()) {
				pushes++;
				COORDINATE t = f.pollSmallest();
				for (DIR d : DIR.ORTHO) {
					f.pushSloppy(t.x(), t.y(), d, 1);
				}
			}
			f.done();
		}
		
		System.out.println("time: " + (System.currentTimeMillis() - now) + "ms " + pushes);
		System.out.println("Filler");
		pushes = 0;
		now = System.currentTimeMillis();
		
		for (int i = 0; i < amount; i++) {
			int sx = r.nextInt(size);
			int sy = r.nextInt(size);
			PathUtilOnline.Filler f = paths.filler;
			f.init(this);
			f.fill(sx, sy);
			
			while(f.hasMore()) {
				pushes ++;
				COORDINATE t = f.poll();
				for (DIR d : DIR.ORTHO) {
					f.fill(t, d);
				}
			}
			f.done();
		}
		
		System.out.println("time: " + (System.currentTimeMillis() - now) + "ms " + pushes);
		System.out.println("Iteration");
		pushes = 0;
		now = System.currentTimeMillis();
		
		for (int i = 0; i < amount; i++) {

			PathUtilOnline.Filler f = paths.filler;
			f.init(this);

			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					f.fill(x, y);
					pushes++;
				}
			}
			f.done();
		}
		System.out.println("time: " + (System.currentTimeMillis() - now) + "ms " + pushes);
		
		
		
	}
	
	
}

class Online{
	
	private int count;
	private static int a = 2500;
	private int amount = a;
	private Path.PathSync p = new Path.PathSync(10000);
	private int size = 768;
	private int i;
	
	int dx;
	int dy;
	
//	private final DEST dest = new DEST() {
//
//		@Override
//		protected boolean isDest(int x, int y) {
//			return x == dx && y == dy;
//		}
//
//		@Override
//		protected float getOptDistance(int x, int y) {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//		
//	};
	
	public Online(){
		
		count = 0;
		Random r = new Random();
		
		PathUtilOnline paths = new PathUtilOnline(size);
		
		final float[][] lookup = new float[size][size]; 
		
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++)
				lookup[y][x] = (r.nextInt(10) == 0) ? -1 : 1;
		
		final Path.COST cm = new Path.COST() {
			@Override
			public double getCost(int fromX, int fromY, int toX, int toY) {
				if (toX < 0 || toX >= size || toY < 0 || toY >= size)
					return -1;
				return lookup[toY][toX];
			}
		};
		
		long heap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		heap /= 1000000;
		System.out.println("---------------------");
		System.out.println("       ONLINE        ");
		System.out.println("---------------------");
		System.out.println("starting processing of " + amount + " paths, heap at: " + heap + "mb");
		
		
		long now = System.currentTimeMillis();
		
		i = 0;
		
		while (amount >= 0){
			
			int sx = r.nextInt(size);
			int sy = r.nextInt(size);
			int dy = r.nextInt(size);
			int dx = r.nextInt(size);

			if (paths.astar.getShortest(p, cm, sx, sy, dx, dy)){
				amount --;
				i++;
				if (i > 300){
					System.out.println("amount: " + amount + " ...");
					i = 0;
				}
				
			}
			
		}
		

		System.out.println("count: " + count);
		System.out.println("time: " + (System.currentTimeMillis() - now) + "ms");
		System.out.println("p/s: " + a*1000/(System.currentTimeMillis() - now));
		long heapSize = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		heapSize /= 1000000;
		System.out.println("heapsize: " + heapSize + "mb" + " (+" + (heapSize - heap) + ")");
	}
}
