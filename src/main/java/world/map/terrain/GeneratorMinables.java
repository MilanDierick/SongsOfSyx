package world.map.terrain;

import static world.WORLD.*;

import init.biomes.TERRAIN;
import init.biomes.TERRAINS;
import init.resources.Minable;
import init.resources.RESOURCES;
import snake2d.util.datatypes.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Tree;
import world.WORLD;
import world.map.pathing.WTRAV;

class GeneratorMinables{

	private final Tree<MNode> tree = new Tree<MNode>(WORLD.TAREA()) {
		@Override
		protected boolean isGreaterThan(MNode current, MNode cmp) {
			return current.value > cmp.value;
		}
	};
	
	GeneratorMinables(){
		
		WORLD.MINERALS().clear();
		
		double am = 0;
		
		for (COORDINATE c : TBOUNDS()) {
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
		if (!WTRAV.isGoodLandTile(tx, ty))
			return false;
		if (FOREST().amount.get(tx, ty) == 1.0)
			return false;
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d = DIR.ORTHO.get(di);
			if (WTRAV.isGoodLandTile(tx+d.x(), ty+d.y()))
				return true;
		}
		return false;
		
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
				if (canPlace(x, y) && !WORLD.MINERALS().is(x, y)) {
					double v = m.terrain(TERRAINS.world.get(x, y))*2;
					for (DIR d : DIR.ORTHO) {
						v += m.terrain(TERRAINS.world.get(x, y, d))*2;
					}
					v /= 6;
					
					if (v > 0) {
						tree.add(new MNode(x, y, v));
					}
				}
			}
		}
		
		while(tot > 0 && tree.hasMore()) {
			MNode c = tree.pollGreatest();
			
			if (WORLD.MINERALS().is(c))
				continue;
			
			WORLD.MINERALS().set(c, m);
			tot--;
			
			if (tot > 0) {
				for (int di = 0; di < DIR.ORTHO.size() && tot > 0; di++) {
					int dx = c.x()+DIR.ORTHO.get(di).x();
					int dy = c.y()+DIR.ORTHO.get(di).y();
					if (canPlace(dx, dy) &&  !WORLD.MINERALS().is(dx, dy)) {
						if (RND.rBoolean()) {
							WORLD.MINERALS().set(dx, dy, m);
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