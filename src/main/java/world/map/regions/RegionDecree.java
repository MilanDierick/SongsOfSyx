package world.map.regions;

import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import util.info.INFO;

public abstract class RegionDecree implements INT_OE<Region>{

	private LIST<RegionFactor> affects = new ArrayList<>(0);
	public abstract int cost(Region r);
	
	RegionDecree(){
		REGIOND.decrees.add(this);
	}
	
	@Override
	public int min(Region t) {
		return 0;
	}
	
	
	public LIST<RegionFactor> affects(){
		return affects;
	}
	
	void connect(RegionFactors stat, double amount) {
		connect(stat, 1, amount-1);
		
	}
	
	void connect(RegionFactors stat, double base, double inc) {
		
		RegionFactor f = new RegionFactor() {
			
			@Override
			public RegionFactors stat() {
				return stat;
			}
			
			@Override
			public INFO info() {
				return RegionDecree.this.info();
			}
			
			@Override
			public double factorPerPoint() {
				return inc;
			}
			
			@Override
			public double getD(Region r) {
				double d = base + inc*RegionDecree.this.get(r)*REGIOND.OWNER().deficiency.getD(r);
				if (d < 0)
					d = 0;
				return d;
			}

			@Override
			public double next(Region r) {
				return getD(r);
			}
		};
		
		this.affects = this.affects.join(f);
		stat.addFactor(f);
		
	}
	
	private static final INT_OE<Region> dummy = new INT_OE<Region>() {

		@Override
		public int get(Region t) {
			return 0;
		}

		@Override
		public int min(Region t) {
			return 0;
		}

		@Override
		public int max(Region t) {
			return 0;
		}

		@Override
		public void set(Region t, int i) {
			
		}
	
	};
	
	static class RegionDecreeImp extends RegionDecree {

		private final INT_OE<Region> data;
		private final INT_OE<Region> master;
		private final INFO info;
		private final int cost;
		
		public RegionDecreeImp(INT_OE<Region> data, int cost, CharSequence name, CharSequence desc) {
			this(data, dummy, cost, name, desc);
			
		}
		
		public RegionDecreeImp(INT_OE<Region> data, INT_OE<Region> master, int cost, CharSequence name, CharSequence desc) {
			info = new INFO(name, desc);
			this.data = data;
			this.cost = cost;
			this.master = master;
			
		}
		
		@Override
		public void set(Region t, int i) {
			REGIOND.OWNER().adminPoints.inc(t, -get(t)*cost(t));
			master.inc(t, -get(t)*cost(t));
			data.set(t, i);
			REGIOND.OWNER().adminPoints.inc(t, get(t)*cost(t));
			master.inc(t, get(t)*cost(t));
			if (t.realm() != null)
				t.realm().recount();
		}

		@Override
		public int get(Region t) {
			return data.get(t);
		}

		@Override
		public int max(Region t) {
			return data.max(t);
		}

		@Override
		public int cost(Region r) {
			
			return cost;
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
	}
	
}
