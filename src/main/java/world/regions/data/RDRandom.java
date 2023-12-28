package world.regions.data;

import game.values.GVALUES;
import snake2d.util.misc.ACTION.ACTION_O;
import util.data.DOUBLE_O;
import util.data.DataRandom;
import world.regions.Region;
import world.regions.data.RD.RDInit;

public final class RDRandom {


	private final DataRandom<Region> ran;
	
	RDRandom(RDInit init){
		
		ran = new DataRandom<Region>(init.count, 4);
		
		int[] pos = new int[] {2,3,4,5,6,7,8,9,15,30,50,100,200,2000};
		
		
		
		for (int i : pos) {
			DOUBLE_O<Region> vv = new DOUBLE_O<Region>() {
				
				@Override
				public double getD(Region reg) {
					return RDRandom.this.get(reg, 0, 31)%(i) == 0 ?  1 : 0;
				}
				
			};
			GVALUES.REGION.push("RANDOM_ONE_IN_" + i, "Random 1:" + i, vv, false);
		}
		
		init.gens.add(new ACTION_O<Region>() {
			
			@Override
			public void exe(Region reg) {
				ran.randomize(reg);
			}
		});
		
	}
	
	public int get(Region r, int startBit, int bits) {
		return ran.get(r, startBit, bits);
	}

	
}
