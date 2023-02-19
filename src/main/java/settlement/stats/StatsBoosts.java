package settlement.stats;

import init.boostable.*;
import init.boostable.BOOSTER_COLLECTION.BOOSTER_COLLECTION_IMP;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.*;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public final class StatsBoosts extends BOOSTER_COLLECTION_IMP{

	private final ArrayList<LinkedList<StatBooster>> adders = new ArrayList<>(BOOSTABLES.all().size());
	private final ArrayList<LinkedList<StatBooster>> muls = new ArrayList<>(BOOSTABLES.all().size());
	private final ArrayList<LinkedList<StatBooster>> all = new ArrayList<>(BOOSTABLES.all().size());
	
	StatsBoosts(Init init) {
		super("stats");

		for (@SuppressWarnings("unused")
		BOOSTABLE b : BOOSTABLES.all()) {
			adders.add(new LinkedList<StatBooster>());
			muls.add(new LinkedList<StatBooster>());
			all.add(new LinkedList<StatBooster>());
		}

		for (StatBooster b : init.boosts) {
			
			all.get(b.boost.boost.index).add(b);
			if (b.boost.isMul())
				muls.get(b.boost.boost.index).add(b);
			else
				adders.get(b.boost.boost.index).add(b);
			
			init(b.boost,1);
		}

	}

	public LIST<StatBooster> all2(BOOSTABLE b) {
		return all.get(b.index);
	}
	
	public LIST<StatBooster> muls(BOOSTABLE b) {
		return muls.get(b.index);
	}

	public double mul(BOOSTABLE b, Induvidual v) {
		if (b == null)
			return 1;
		double mul = 1;
		for (StatBooster bb : muls.get(b.index)) {
			mul *= bb.value(v);
		}
		return mul;
	}
	
	public double mul(BOOSTABLE b, HCLASS c, Race r) {
		if (b == null)
			return 0;
		double mul = 1;
		for (StatBooster bb : muls.get(b.index)) {
			mul *= bb.value(c, r);
		}
		return mul;
	}

	public double mul(BOOSTABLE b, Div v) {
		if (b == null)
			return 0;
		double mul = 1;
		for (StatBooster bb : muls.get(b.index)) {
			mul *= bb.value(v);
		}
		return mul;
	}
	
	public LIST<StatBooster> adders(BOOSTABLE b) {
		return adders.get(b.index);
	}
	
	public double add(BOOSTABLE b, Induvidual v) {
		if (b == null)
			return 0;
		double add = 0;
		for (StatBooster bb : adders.get(b.index)) {
			add += bb.value(v);
		}
		return add;
	}
	
	public double add(BOOSTABLE b, HCLASS c, Race r) {
		if (b == null)
			return 0;
		double add = 0;
		for (StatBooster bb : adders.get(b.index)) {
			add += bb.value(c, r);
		}
		return add;
	}

	public double add(BOOSTABLE b, Div v) {
		if (b == null)
			return 0;
		double add = 0;
		for (StatBooster bb : adders.get(b.index)) {
			add += bb.value(v);
		}
		return add;
	}
	

	public static abstract class StatBooster {
		
		public final BBoost boost;
		private final CharSequence name;
		public final double start;
		public final double delta;
		
		StatBooster(Init init, CharSequence name, BBoost boost){
			this.boost = boost;
			this.name = name;
			init.boosts.add(this);
			if (boost.isMul()) {
				start = 1.0;
				delta = boost.value()-1.0;
			}else {
				start = 0.0;
				delta = boost.value();
			}
			
		}
		
		public final double value(Induvidual i) {
			return start+delta*pvalue(i);
		}
		
		public final double value(HCLASS c, Race r) {
			return start+delta*pvalue(c,r);
		}
		
		public final double value(Div v) {
			return start+delta*pvalue(v);
		}

		public CharSequence name() {
			return name;
		}

		protected abstract double pvalue(Induvidual v);

		protected abstract double pvalue(HCLASS c, Race r);

		protected abstract double pvalue(Div v);
		
	}

	
	static final class StatBoosterStat extends StatBooster {

		private final STAT stat;
		private final double mul;

		StatBoosterStat(Init init, CharSequence name, STAT stat, BBoost boost, double mul) {
			super(init, name, boost);
			this.stat = stat;
			stat.boosts.add(this);
			this.mul = mul;
		}
		
		StatBoosterStat(Init init, STAT stat, BBoost boost, double mul) {
			this(init, stat.info().name, stat, boost, mul);
		}
		
		StatBoosterStat(Init init, CharSequence name, STAT stat, BBoost boost) {
			this(init, name, stat, boost, 1);
		}
		
		StatBoosterStat(Init init, STAT stat, BBoost boost) {
			this(init, stat.info().name, stat, boost);
		}

		@Override
		public double pvalue(Induvidual v) {
			return stat.indu().getD(v)*mul;
		}

		@Override
		public double pvalue(HCLASS c, Race r) {
			return stat.data(c).getD(r)*mul;
		}

		@Override
		public double pvalue(Div v) {
			return stat.div().getD(v)*mul;
		}


	}
	
	public void hover(GUI_BOX box, BOOSTABLE b, Induvidual a) {
		for (StatBooster boo : adders(b)) {
			hover(box, b, boo, boo.value(a));
		}
		for (StatBooster boo : muls(b)) {
			hover(box, b, boo, boo.value(a));
		}
		
	}
	
	private static void hover(GUI_BOX box, BOOSTABLE b, StatBooster boo, double value) {
		GBox bb =(GBox) box;
		bb.NL();
		bb.text(boo.name());
		bb.tab(6);
		if (boo.boost.isMul()) {

			GText t = bb.text();
			t.add('*');
			bb.add(GFORMAT.f1(t, value));
			bb.tab(9);
			t = bb.text();
			double min = boo.boost.value() < 1 ? boo.boost.value() : 1;
			double max = boo.boost.value() > 1 ? boo.boost.value() : 1;
			t.add(min, 2).s().add('-').s().add(max,2);
			bb.add(t);
			
		}else {
			
			double min = boo.boost.value() < 0 ? boo.boost.value() : 0;
			double max = boo.boost.value() > 0 ? boo.boost.value() : 0;
			bb.add(GFORMAT.f0(bb.text(), value));
			bb.tab(9);
			GText t = bb.text();
			t.add(min, 2).s().add('-').s().add(max,2);
			bb.add(t);
		}
		bb.NL();
	}

}
