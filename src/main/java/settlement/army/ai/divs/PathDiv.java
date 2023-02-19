package settlement.army.ai.divs;

import java.io.IOException;

import init.C;
import settlement.army.Army;
import settlement.army.formation.DivPlacability;
import settlement.army.order.Copyable;
import snake2d.Path;
import snake2d.util.datatypes.*;
import snake2d.util.file.*;

public final class PathDiv implements COORDINATE, SAVABLE, Copyable<PathDiv>{

	private final static int size = 128;
	private final static VectorImp vec = new VectorImp();
	
	private final int[] coos = new int[size*2];
	private int current = 0;
	private int length = 0;
	private final Coo finalDest = new Coo(-1,-1);
	private boolean isComplete;
	private int tilesToDest = 0;
	
	public PathDiv() {
		
	}

	@Override
	public void save(FilePutter file) {
		file.is(coos);
		file.i(current);
		file.i(length);
		finalDest.save(file);
		file.bool(isComplete);
		file.i(tilesToDest);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		file.is(coos);
		current = file.i();
		length = file.i();
		finalDest.load(file);
		isComplete = file.bool();
		tilesToDest = file.i();
	}
	
	@Override
	public void clear() {
		current = 0;
		length = 0;
		finalDest.set(-1,-1);
		tilesToDest = 0;
	}

	@Override
	public int x() {
		return coos[current];
	}

	@Override
	public int y() {
		return coos[current+size];
	}
	
	private boolean canWalkTheLine2(int sx, int sy, int dx, int dy, double max, PathCost cost, Army a) {
		double c = 0;
		double l = vec.set(sx, sy, dx, dy);
		int steps = (int) Math.abs(l/C.TILE_SIZE);
		for (int i = 1; i <= steps; i++) {
			int x = (int) (sx + i*C.TILE_SIZE*vec.nX());
			int y = (int) (sy + i*C.TILE_SIZE*vec.nY());
			if (DivPlacability.pixelIsBlocked(x, y, C.TILE_SIZE, a))
				return false;
			if (i < steps) {
				int nx = (int) (sx + (i+1)*C.TILE_SIZE*vec.nX());
				int ny = (int) (sy + (i+1)*C.TILE_SIZE*vec.nY());
				if (!DivPlacability.checkPixelStep(x, y, nx, ny, C.TILE_SIZE, a))
					return false;
			}
			c += getCost(x, y, cost);
			if (c > max) {
				return false;
			}
				
			
		}
		
		return true;
		
	}
	
	void init(int startPX, int startPY, Path p, int ftDestX, int ftDestY, PathCost cost, Army a){
		
		
		finalDest.set(ftDestX, ftDestY);
		coos[0] = startPX; 
		coos[size] = startPY; 
		length = 1;
		current = 0;
		int currentI = 0;
		if (p.hasNext()) {
			p.setNext();
			int distance = 1;
			
	
			while(++currentI < size) {
				int sx = coos[currentI-1];
				int sy = coos[currentI-1+size];
				double costt = getCost(sx, sy, cost);
				while(p.hasNext() && distance < 32) {
					int px = p.x();
					int py = p.y();
					p.setNext();
					int dx = (p.x()<<C.T_SCROLL) + C.TILE_SIZEH;
					int dy = (p.y()<<C.T_SCROLL) + C.TILE_SIZEH;
					costt += getCost(dx, dy, cost)*((px != p.x() && py != p.y()) ? C.SQR2 : 1.0);
					
					if (!canWalkTheLine2(sx, sy, dx, dy, costt, cost, a)) {
						p.setPrev();
						
						break;
					}
					distance++;
				}
				int dx = (p.x()<<C.T_SCROLL) + C.TILE_SIZEH;
				int dy = (p.y()<<C.T_SCROLL) + C.TILE_SIZEH;
				double d = vec.set(sx, sy, dx, dy);
				length ++;
				if (d < C.TILE_SIZE+C.TILE_SIZEH) {
					coos[currentI] = dx;
					coos[currentI+size] = dy;
					if (!p.hasNext())
						break;
					p.setNext();
				}else {
					coos[currentI] = (int) (sx + vec.nX()*C.TILE_SIZE);
					coos[currentI+size] = (int) (sy + vec.nY()*C.TILE_SIZE);
				}
				if (distance > 0)
					distance--;
			};
		}
		
		setCurrentI(length-1);
		
		setCurrentI(0);
		
		current = 0;
		
		isComplete = p.isCompleate() && length < size;
		
		tilesToDest = p.lengthTotal()-p.getCurrentI()+length();
		
	}
	
	public int length() {
		return length;
	}
	
	public int currentI() {
		return current;
	}
	
	public void setCurrentI(int i ) {
		if (i < 0 || i >= length)
			throw new RuntimeException(i + " " + length);
		current = i;
	}
	
	public void currentIInc(int d) {
		setCurrentI(current+d);
	}
	
	public boolean isDest() {
		return current >= length-1;
	}
	
	public COORDINATE finalTDest() {
		return finalDest;
	}
	
	public boolean isComplete() {
		return isComplete;
	}
	
	public int tilesToDest() {
		return tilesToDest-currentI();
	}
	
	@Override
	public void copy(PathDiv toBeCopied) {
		for (int i = 0; i < coos.length; i++) {
			coos[i] = toBeCopied.coos[i];
		}
		current = toBeCopied.current;
		length = toBeCopied.length;
		tilesToDest = toBeCopied.tilesToDest;
		finalDest.set(toBeCopied.finalDest);
		isComplete = toBeCopied.isComplete;
		current = toBeCopied.current;
		
		
	}
	
	private static double getCost(int x1, int y1, PathCost m) {
		return m.cost(x1>>C.T_SCROLL, y1>>C.T_SCROLL);
	}
	
//	private static boolean checkStep(int fx, int fy, int tox, int toy, int tz) {
//		{
//			fx = (fx) >> C.T_SCROLL;
//			fy = (fy) >> C.T_SCROLL;
//			int tx = (tox) >> C.T_SCROLL;
//			int ty = (toy) >> C.T_SCROLL;
//			if (tx != fx || ty != fy)
//				if (player.getCost(fx, fy, tx, ty) < 0)
//					return false;
//		}
//		return true;
//
//	}

}
