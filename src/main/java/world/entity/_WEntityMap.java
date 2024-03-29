package world.entity;

import init.C;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.sets.*;
import world.WORLD;
import world.regions.Region;
import world.regions.WREGIONS;

class _WEntityMap {

	private final _QuadrantArray[][] quadrants;
	private final WEntity[] region = new WEntity[WREGIONS.MAX];
	private final int qMaxX;
	private final int qMaxY;
	private final int gridSize = C.TILE_SIZE*16;

	public _WEntityMap(int mapSizeX, int mapSizeY){

		qMaxX = mapSizeX/gridSize;
		qMaxY = mapSizeY/gridSize;
		
		quadrants = new _QuadrantArray[qMaxX][qMaxY];
		
		for (int y = 0; y < quadrants.length; y++){
			for (int x = 0; x < quadrants[0].length; x++){
				quadrants[y][x] = new _QuadrantArray();
			}
		}
		
	}
	
	public void add(WEntity e){
		
		if (!isOut(e.gridX, e.gridY))
			throw new RuntimeException();
		
		e.gridX = (short) (e.body().x1()/gridSize);
		e.gridY = (short) (e.body().y1()/gridSize);
		
		
		if (isOut(e.gridX, e.gridY))
			return;
		
		quadrants[e.gridY][e.gridX].add(e);
		
		e.regionI = -1;
		Region reg = WORLD.REGIONS().map.get(e.ctx(), e.cty());
		if (reg != null) {
			e.regionI = (short) reg.index();
			WEntity rn = region[reg.index()];
			region[reg.index()] = e;
			e.regionNext = rn;
		}
	}
	
	public void remove(WEntity e){
		
		if (isOut(e.gridX, e.gridY))
			return;
		
		quadrants[e.gridY][e.gridX].remove(e);
		e.gridY = -1;
		if (e.regionI != -1) {
			
			WEntity current = region[e.regionI];
			region[e.regionI] = null;
			while(current != null) {
				if (current != e) {
					WEntity rn = region[e.regionI];
					region[e.regionI] = current;
					WEntity next = current.regionNext;
					current.regionNext = rn;
					current = next;
				}else
					current = current.regionNext;
			}
			e.regionNext = null;
			e.regionI = -1;
		}
	}
	
	public WEntity regFirst(Region reg) {
		return region[reg.index()];
	}

	public void move(WEntity e) {
		
		short gridX = (short) (e.body().x1()/gridSize);
		short gridY = (short) (e.body().y1()/gridSize);
		Region reg = WORLD.REGIONS().map.get(e.ctx(), e.cty());
		int ri = reg == null ? -1 : reg.index();
		
		if (e.gridX != gridX || e.gridY != gridY || ri != e.regionI) {
			remove(e);
			add(e);
		}
		
		
	}
	
	private boolean isOut(int qx1, int qy1){
		return qx1 >= qMaxX || qy1 >= qMaxY || qx1 < 0 || qy1 < 0;
	}
	
	void fill(RECTANGLE area, LISTE<WEntity> result) {
		fill(area.x1(), area.x2(), area.y1(), area.y2(), result);
			
	}
	
	void fill(RECTANGLE area, Tree<WEntity> result) {
		fill(area.x1(), area.x2(), area.y1(), area.y2(), result);
			
	}
	
	void fill(int x1, int x2, int y1, int y2, ADDABLE<WEntity> result) {
		
		int min = 3*C.TILE_SIZE;
		
		int qx1 = (x1-min)/gridSize;
		if (qx1 < 0)
			qx1 = 0;
		int qy1 = (y1-min)/gridSize;
		if (qy1 < 0)
			qy1 = 0;
		int qx2 = (x2+min)/gridSize;
		if (qx2 >= qMaxX)
			qx2 = qMaxX -1;
		int qy2 = (y2+min)/gridSize;
		if (qy2 >= qMaxY)
			qy2 = qMaxY -1;
		
		for (int y = qy1; y <= qy2; y++){
			for (int x = qx1; x <= qx2; x++){
				for (WEntity e : quadrants[y][x]) {
					if (e.body().touches(x1, x2, y1, y2)) {
						if (!result.hasRoom())
							return;
						result.add(e);
					}
				}
			}
		}
			
	}
	
	void fill(int x, int y, ADDABLE<WEntity> result) {
		fill(x, x, y, y, result);
	}
	
	void clear(){
		for (int y = 0; y < quadrants.length; y++){
			for (int x = 0; x < quadrants[0].length; x++){
				quadrants[y][x].clear();
			}
		}
		for (int i = 0; i < region.length; i++)
			region[i] = null;
	}

	
}
