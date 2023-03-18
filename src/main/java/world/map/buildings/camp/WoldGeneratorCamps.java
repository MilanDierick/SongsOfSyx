package world.map.buildings.camp;

import static world.World.*;

import init.biomes.*;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Tree;
import world.World;

public final class WoldGeneratorCamps {
	
	public WoldGeneratorCamps() {
		
		WorldCamp cc = World.camps();
		
		ArrayList<WW> spots = new ArrayList<>(cc.types.size());
		for (int i = 0; i < cc.types.size(); i++)
			spots.add(new WW(cc.types.get(i)));
		
		
		for (COORDINATE c : TBOUNDS()) {
			
			if (!REGIONS().haser.is(c))
				continue;
			if (World.REGIONS().isCentre.is(c))
				continue;
			if (World.MOUNTAIN().haser.is(c.x(), c.y()))
				continue;
			if (World.WATER().has.is(c))
				continue;
			if (World.FOREST().amount.get(c) > 0.25)
				continue;
			
			CLIMATE cl = CLIMATE().getter.get(c);
			for (WW w : spots) {
				w.add(c.x(), c.y(), cl);
			}
			
		}
		
		other:
		while(!spots.isEmpty()) {
			WW w = spots.get(RND.rInt(spots.size()));
			if (w.am <= 0 || !w.spots.hasMore()) {
				spots.remove(w);
				continue;
			}
			
			Coovalue c = w.spots.pollGreatest();
			
			for (DIR d : DIR.ALLC) {
				if (World.BUILDINGS().camp.map.get(c.tx, c.ty, d) != null) {
					continue other;
				}
			}
			
			World.camps().create(c.tx, c.ty, w.w, RND.rFloat());
			w.am--;
			
			
		}
	}
	
	private static class WW {
		
		double am = 0;
		private final Tree<Coovalue> spots = new Tree<Coovalue>(WorldCamp.MAX) {

			@Override
			protected boolean isGreaterThan(Coovalue current, Coovalue cmp) {
				return current.value > cmp.value;
			}
		};
		public final WCampType w;
		WW(WCampType w){
			this.w = w;
		}
		
		void add(int tx, int ty, CLIMATE cl) {
			double res = 0;
			for (TERRAIN t : TERRAINS.ALL()) {
				res += w.climates[cl.index()]*w.terrains[t.index()]*t.value(tx, ty);
			}
			am+=res;
			
			if (!spots.hasRoom()) {
				if (spots.smallest().value < res)
					spots.pollSmallest();
				else
					return;
			}
			
			
			Coovalue v = new Coovalue();
			v.value = res*RND.rFloat();
			v.tx = (short) tx;
			v.ty = (short) ty;
			spots.add(v);
		}
		
	}
	
	private static class Coovalue {
		short tx;
		short ty;
		double value;
	}
	


}
