package world.map.regions;

import static world.World.*;

import snake2d.util.rnd.RND;
import world.World;

final class GeneratorCenter {


	
	public GeneratorCenter() {
		
	}


	
	static void init(Region a) {
		if (a == null)
			return;
		if (a.isWater())
			return;
		if (a.area == 0)
			return;
		
		if (CapitolPlacablity.region(a.cx(), a.cy()) != null)
			System.err.println(CapitolPlacablity.region(a.cx(), a.cy()));
		
		for (int i = 0; i < 5; i++) {
			int x = a.cx() + (int) (RND.rSign()*RND.rInt(a.bounds.width()));
			int y = a.cy() + (int) (RND.rSign()*RND.rInt(a.bounds.height()));
			if (REGIONS().setter.is(x, y, a) && test(x, y, a)) {
				a.centreSet(x, y);
				break;
			}
		}
		
	}
	
	private static boolean test(int tx, int ty, Region r) {
		
		if (r.index() == 0)
			return true;
		
		if (!World.REGIONS().setter.is(tx,ty,r))
			return false;
		if (CapitolPlacablity.region(tx, ty) != null)
			return false;
		
		for (int dy = -1; dy < 3; dy++) {
			for (int dx = -1; dx < 3; dx++) {
				int x = tx+dx;
				int y = ty+dy;
				if (x < 1 || x >= World.TWIDTH()-1 || y < 1 || y >= World.THEIGHT()-1)
					return false;
				if (!World.REGIONS().setter.is(tx+dx,ty+dy,r))
					return false;
			}
		}
		return true;
		
	}
	
}
