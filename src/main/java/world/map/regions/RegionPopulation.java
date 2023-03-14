package world.map.regions;

import init.D;
import init.config.Config;
import snake2d.util.misc.CLAMP;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import world.map.regions.RegionFactor.RegionFactorImp;

public final class RegionPopulation {

	public final int MAX;
	public final double MAXI;
	final INT_OE<Region> total;
	public final DOUBLE_OE<Region> maxbase;

	private static CharSequence ¤¤Population = "¤Population";
	private static CharSequence ¤¤PopulationTotal = "¤Total Population";
	private static CharSequence ¤¤Capacity = "¤Capacity";
	private static CharSequence ¤¤CapacityD = "¤the total subject capacity.";
	private static CharSequence ¤¤Base = "¤Capacity Base";
	private static CharSequence ¤¤BaseD = "¤The base of the total subject capacity. Determined by size and fertility.";
	public final RegionFactors capacity = new RegionFactors(¤¤Capacity, ¤¤CapacityD);
	
	static {
		D.ts(RegionPopulation.class);
	}

	RegionPopulation(RegionInit init) {
		
		MAX = Config.WORLD.TILE_POPULATION * GeneratorAssigner.maxSize;
		MAXI = 1.0/MAX;
		new RegionFactorImp(capacity, ¤¤Base, ¤¤BaseD) {

			@Override
			public double getD(Region r) {
				return (int)(MAX*maxbase.getD(r));
			}

			@Override
			public double next(Region r) {
				return getD(r);
			}
			
		};
		total = init.count.new DataInt(¤¤PopulationTotal, ¤¤Population) {
			@Override
			public int max(Region t) {
				return (int)capacity.getD(t);
			}
		};
		
		
		maxbase = init.count.new DataByte();
		
		
	}

	public INT_O<Region> total() {
		return total;
	}
	
	public double popValue(Region r) {
		return CLAMP.d(total.get(r)/(MAX*maxbase.getD(r)*2), 0, 1);
	}

	public DOUBLE_OE<Region> base() {
		return maxbase;
	}

}
