package settlement.room.main.throne;

import static settlement.main.SETT.*;

import init.RES;
import settlement.main.SETT;

public final class Initer {

	Initer(THRONE t){
		
	}
	
	
	public boolean placebleWhole(int x1, int y1, int rot) {
		int x2 = x1 + Sprite.width(rot);
		int y2 = y1 + Sprite.height(rot);
		
		for (int y = y1; y < y2; y++) {
			for (int x = x1; x < x2; x++) {
				if (!placableTile(x, y))
					return false;
			}
		}
		return true;
	}
	
	private boolean placableTile(int tx, int ty) {
		if (JOBS().getter.has(tx, ty))
			return false;
		if (ROOMS().map.is(tx, ty))
			return false;
		if (TERRAIN().TREES.isTree(tx, ty))
			return true;
		if (!TERRAIN().NADA.is(tx, ty) && !TERRAIN().get(tx, ty).roofIs() && !TERRAIN().get(tx, ty).clearing().isEasilyCleared())
			return false;
		return true;
	}
	
	public void markArround(int tx, int ty) {
		int i = 0;
		while (RES.circle().radius(i) < 100) {
			int x = tx+RES.circle().get(i).x();
			int y = ty+RES.circle().get(i).y();
			
			if (placebleWhole(x, y, 0)) {
				SETT.ROOMS().THRONE.setInstance(tx, ty);
				return;
			}
			i++;
		}
		
		SETT.ROOMS().THRONE.setInstance(tx, ty);
		//throw new RuntimeException();
	}
	
	
	public void place(int x1, int y1, int rot) {
		new Instance(x1, y1, rot);
	}
	
}
