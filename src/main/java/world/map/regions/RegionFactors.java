package world.map.regions;

import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O;
import util.info.INFO;

public class RegionFactors implements DOUBLE_O<Region>{
	
	private LIST<RegionFactor> factors = new ArrayList<>();
	private final INFO info;
	
	RegionFactors(INFO info){
		this.info = info;
	}
	
	RegionFactors(CharSequence name, CharSequence desc){
		this(name, desc, false);
	}
	
	RegionFactors(CharSequence name, CharSequence desc, boolean isInt){
		this.info = new INFO(name, desc);
	}

	@Override
	public INFO info() {
		return info;
	}
	
	public LIST<RegionFactor> factors(){
		return factors;
	}

	@Override
	public double getD(Region t) {
		double d = 1;
		for (RegionFactor f : factors)
			d *= f.getD(t);
		int k = (int) (d*255);
		return k/255.0;
	}
	
	public double next(Region r) {
		double d = 1;
		for (RegionFactor f : factors)
			d *= f.next(r);
		int k = (int) (d*255);
		return k/255.0;
	}
	
	void addFactor(RegionFactor f) {
		factors = factors.join(f);
		if (f.info() == null)
			throw new RuntimeException();
	}
	
	RegionFactor makeFactor(double base, double mul) {
		return new RegionFactor.RegionFactorImp(null, info) {
			@Override
			public double getD(Region r) {
				return base + getD(r)*mul;
			}

			@Override
			public double next(Region r) {
				return base + next(r)*mul;
			}
		};
	}

}