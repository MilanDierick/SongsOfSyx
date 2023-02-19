package world.map.regions;

import util.data.DOUBLE_O;
import util.info.INFO;

public interface RegionFactor extends DOUBLE_O<Region>{
	
	public default RegionFactors stat() {
		return null;
	}
	public default double factorPerPoint() {
		return 0;
	}
	
	public double next(Region r);
	
	static abstract class RegionFactorImp implements RegionFactor{

		private final RegionFactors stat;
		private final INFO info;
		
		public RegionFactorImp(RegionFactors stat, INFO info) {
			this.stat = stat;
			this.info = info;
			stat.addFactor(this);
		}
		
		public RegionFactorImp(RegionFactors stat, CharSequence name, CharSequence desc) {
			this(stat, new INFO(name, desc));
		}
		
		@Override
		public RegionFactors stat() {
			return stat;
		}

		@Override
		public double factorPerPoint() {
			return 0;
		}

		@Override
		public INFO info() {
			return info;
		}
		
		
		
	}

}