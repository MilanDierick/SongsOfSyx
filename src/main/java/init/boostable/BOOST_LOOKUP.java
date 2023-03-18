package init.boostable;

import java.util.Arrays;
import java.util.LinkedList;

public interface BOOST_LOOKUP {

	public double minAdd(BOOSTABLE b);
	public double maxAdd(BOOSTABLE b);
	public double minMul(BOOSTABLE b);
	public double maxMul(BOOSTABLE b);
	
	
	public CharSequence name();
	
	public static class BOOSTER_LOOKUP_IMP implements BOOST_LOOKUP {

		protected final double[] max = new double[BOOSTABLES.all().size()];
		protected final double[] min = new double[BOOSTABLES.all().size()];
		protected final double[] maxAdd = new double[BOOSTABLES.all().size()];
		protected final double[] minAdd = new double[BOOSTABLES.all().size()];
		private CharSequence name;
		
		
		protected BOOSTER_LOOKUP_IMP(CharSequence name){
			this.name = name;
			Arrays.fill(max, 1);
			Arrays.fill(min, 1);
		}
		
		protected void init(BOOST_HOLDER bo) {
			init(bo, 1);
			
		}
		
		protected void init(BOOST_HOLDER bo, double mul) {
			for (BBoost b : bo.boosts()) {
				init(b, mul);
			}
		}
		
		protected void init(BBoost b, double mul) {
			int i = b.boostable.index();

			if (b.isMul()) {
				if (b.end < min[i])
					min[i] = b.end;
				if (b.start < min[i])
					min[i] = b.start;
				if (b.end > max[i])
					max[i] = b.end*mul;
				if (b.start > max[i])
					max[i] = b.end*mul;
			}else {
				if (b.end < minAdd[i])
					minAdd[i] = b.end;
				if (b.start < minAdd[i])
					minAdd[i] = b.start;
				if (b.end > maxAdd[i])
					maxAdd[i] = b.end*mul;
				if (b.start > maxAdd[i])
					maxAdd[i] = b.end*mul;
			}
		}
		
		protected void init(BOOST_LOOKUP coll) {
			
			for (int bi = 0; bi < BOOSTABLES.all().size(); bi++) {
				min[bi]*= coll.minMul(BOOSTABLES.all().get(bi));
				max[bi]*= coll.maxMul(BOOSTABLES.all().get(bi));
				minAdd[bi] += coll.minAdd(BOOSTABLES.all().get(bi));
				maxAdd[bi] += coll.maxAdd(BOOSTABLES.all().get(bi));
			}
		}
		
		@Override
		public double minMul(BOOSTABLE b) {
			return min[b.index()];
		}
		
		@Override
		public double minAdd(BOOSTABLE b) {
			return minAdd[b.index()];
		}
		
		@Override
		public double maxMul(BOOSTABLE b) {
			return max[b.index()];
		}
		
		@Override
		public double maxAdd(BOOSTABLE b) {
			return maxAdd[b.index()];
		}

		@Override
		public CharSequence name() {
			return name;
		}
		
		protected LinkedList<BBoost> makeBoosts() {
			LinkedList<BBoost> all = new LinkedList<>();
			for (BOOSTABLE b : BOOSTABLES.all()) {
				if (minAdd(b) < 0 || maxAdd(b) > 0) {
					all.add(new BBoost(b, minAdd(b), maxAdd(b), false));
				}
				if (minMul(b) < 1 || maxMul(b) > 1) {
					all.add(new BBoost(b, minMul(b), maxMul(b), true));
				}
			}
			return all;
		}
		
		protected void makeBoosters(SIMPLE s, boolean player, boolean enemy, boolean cache) {
			for (BBoost b : makeBoosts()) {
				if (b.isMul()) {
					new BBooster.BBoosterSimple(name(), b, player, enemy, cache) {
						@Override
						public double pvalue() {
							return s.mul(b.boostable);
						}
					};
				}else {
					new BBooster.BBoosterSimple(name(), b, player, enemy, cache) {
						@Override
						public double pvalue() {
							return s.add(b.boostable);
						}
					};
				}
			}
		}
	}
	
	public interface SIMPLE extends BOOST_LOOKUP {
		
		public double add(BOOSTABLE b);
		public double mul(BOOSTABLE b);
		public default double tot(BOOSTABLE b) {
			return mul(b)*(1+add(b));
		}
		
	}

}
