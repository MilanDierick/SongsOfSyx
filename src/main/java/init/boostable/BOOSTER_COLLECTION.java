package init.boostable;

import java.util.Arrays;

import snake2d.util.gui.GUI_BOX;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public interface BOOSTER_COLLECTION {

	public double minAdd(BOOSTABLE b);
	public double maxAdd(BOOSTABLE b);
	public double minMul(BOOSTABLE b);
	public double maxMul(BOOSTABLE b);
	public CharSequence name();
	
	
	public static class BOOSTER_COLLECTION_IMP implements BOOSTER_COLLECTION {

		protected final double[] max = new double[BOOSTABLES.all().size()];
		protected final double[] min = new double[BOOSTABLES.all().size()];
		protected final double[] maxAdd = new double[BOOSTABLES.all().size()];
		protected final double[] minAdd = new double[BOOSTABLES.all().size()];
		private CharSequence name;
		
		
		protected BOOSTER_COLLECTION_IMP(CharSequence name){
			this.name = name;
			Arrays.fill(max, 1);
			Arrays.fill(min, 1);
		}
		
		protected void init(BOOSTER bo) {
			init(bo, 1);
			
		}
		
		protected void init(BOOSTER bo, double mul) {
			for (BBoost b : bo.boosts()) {
				init(b, mul);
			}
		}
		
		protected void init(BBoost b, double mul) {
			int i = b.boost.index();
			double v = b.value()*mul;
			if (b.isMul()) {
				if (v < min[i])
					min[i] = v;
				else if (v > max[i])
					max[i] = v;
			}else {
				if (v < minAdd[i])
					minAdd[i] = v;
				else if (v > maxAdd[i])
					maxAdd[i] = v;
			}
		}
		
		protected void init(BOOSTER_COLLECTION coll) {
			
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
	}
	
	public default void hover(GUI_BOX box, BOOSTABLE b, double mul, double add) {
		GBox bb =(GBox) box;
		bb.NL();
		if (maxMul(b) != 1 || minMul(b) != 1) {
			bb.text(name());
			bb.tab(6);
			GText t = bb.text();
			t.add('*');
			bb.add(GFORMAT.f1(t, mul));
			bb.tab(9);
			t = bb.text();
			t.add(minMul(b), 2).s().add('-').s().add(maxMul(b),2);
			bb.add(t);
			bb.NL();
		}
		if (maxAdd(b) != 0 || minAdd(b) != 0) {
			bb.text(name());
			bb.tab(6);
			bb.add(GFORMAT.f0(bb.text(), add));
			bb.tab(9);
			GText t = bb.text();
			t.add(minAdd(b), 2).s().add('-').s().add(maxAdd(b),2);
			bb.add(t);
			bb.NL();
		}
	}

	
	public interface SIMPLE extends BOOSTER_COLLECTION {
		
		public double add(BOOSTABLE b);
		public double mul(BOOSTABLE b);
		public default double tot(BOOSTABLE b) {
			return mul(b)*(1+add(b));
		}

		default void hover(GUI_BOX box, BOOSTABLE b) {
			hover(box, b, mul(b), add(b));
		}
		
	}
	
	public interface OBJECT<T> extends BOOSTER_COLLECTION {
		
		public double add(BOOSTABLE b, T t);
		public double mul(BOOSTABLE b, T t);
		
		public default double tot(BOOSTABLE b, T t) {
			return mul(b,t)*(1+add(b,t));
		}
		
		public default void hover(GUI_BOX box, BOOSTABLE b, T tt) {
			hover(box, b, mul(b, tt), add(b, tt));
		}
	}
}
