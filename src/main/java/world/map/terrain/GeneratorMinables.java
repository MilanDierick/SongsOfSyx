package world.map.terrain;

import static settlement.main.SETT.PHEIGHT;
import static settlement.main.SETT.PWIDTH;
import static world.World.*;
import static world.World.THEIGHT;
import static world.World.TWIDTH;

import init.biomes.TERRAIN;
import init.biomes.TERRAINS;
import init.resources.Minable;
import init.resources.RESOURCES;
import snake2d.util.datatypes.*;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Tree;
import world.World;

class GeneratorMinables{

	private final Tree<MNode> tree = new Tree<MNode>(World.TAREA()) {
		@Override
		protected boolean isGreaterThan(MNode current, MNode cmp) {
			return current.value > cmp.value;
		}
	};
	
	GeneratorMinables(HeightMap height){
		
		double am = 0;
		
		for (COORDINATE c : TBOUNDS()) {
			if (canPlace(PWIDTH, PHEIGHT))
			if (MOUNTAIN().coversTile(c.x(), c.y()))
				continue;
			if (WATER().coversTile.is(c))
				continue;
			if (FOREST().amount.get(c) == 1.0)
				continue;
			am++;
		}
		
		if (am == 0)
			return;
		
		am /= 100*100;
		
		for (Minable m : RESOURCES.minables()) {
			add(m, am);
		}
		
		
	}
	
	private boolean canPlace(int tx, int ty) {
		if (!World.IN_BOUNDS(tx, ty))
			return false;
		if (MOUNTAIN().coversTile(tx, ty))
			return false;
		if (WATER().coversTile.is(tx, ty))
			return false;
		if (FOREST().amount.get(tx, ty) == 1.0)
			return false;
		return true;
	}
	
	private void add(Minable m, double area) {
		
		double tot = 0;
		for (TERRAIN t : TERRAINS.ALL()) {
			tot += m.terrain(t);
		}
		
		tot /= TERRAINS.ALL().size();
		
		tot *= area*100;
		
		if (tot < 1)
			tot = 1;
		
		tree.clear();
		
		for (int y = 0; y < THEIGHT(); y++) {
			for (int x = 0; x < TWIDTH(); x++) {
				if (canPlace(x, y) && !World.MINERALS().is(x, y)) {
					double v = m.terrain(TERRAINS.world.get(x, y));
					if (v > 0) {
						tree.add(new MNode(x, y, v));
					}
				}
			}
		}
		
		while(tot > 0 && tree.hasMore()) {
			MNode c = tree.pollGreatest();
			
			if (World.MINERALS().is(c))
				continue;
			
			World.MINERALS().set(c, m);
			tot--;
			
			if (tot > 0) {
				for (int di = 0; di < DIR.ORTHO.size() && tot > 0; di++) {
					int dx = c.x()+DIR.ORTHO.get(di).x();
					int dy = c.y()+DIR.ORTHO.get(di).y();
					if (canPlace(dx, dy) &&  !World.MINERALS().is(dx, dy)) {
						double r = m.occurance * m.terrain(TERRAINS.world.get(dx, dy));
						if (r >= RND.rFloat()) {
							World.MINERALS().set(dx, dy, m);
							tot--;
						}
					}
					
				}
			}
			
			
			
		}
	}
	
	private static class MNode extends ShortCoo {
		
		private static final long serialVersionUID = 1L;
		final float value;
		
		MNode(int x, int y, double value){
			super(x, y);
			this.value = (float) (value += Math.pow(RND.rFloat(), 2));
		}
		
	}
	

}