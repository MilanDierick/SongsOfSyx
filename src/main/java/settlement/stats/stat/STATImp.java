package settlement.stats.stat;

import init.race.RACES;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.*;
import settlement.stats.StatsInit.StatUpdatable;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.statistics.HISTORY_INT.HISTORY_INT_OBJECT;

public abstract class STATImp extends STAT implements StatUpdatable {


	private final SettStatistics stats;
	private final INT_OE<Induvidual> indu;

	public STATImp(String key, StatsInit init) {
		this(key, init, null);
	}

	public STATImp(String key, StatsInit init, StatInfo info) {
		super(key, init, info);

		stats = new SettStatistics(init, info) {

			@Override
			protected int popDivider(HCLASS c, Race r, int daysback) {
				return (int) pdivider(c, r, daysback);
			}

			@Override
			public int dataDivider() {
				return STATImp.this.dataDivider();
			}
		};

		indu = new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				return (int) (stats.data(t.clas()).getD(t.race()) * dataDivider());
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				return STATImp.this.dataDivider();
			}

			@Override
			public void set(Induvidual t, int i) {

			}

		};
		init.upers.add(this);

	}
	
	@Override
	public int pdivider(HCLASS c, Race r, int daysback) {
		return STATS.POP().POP.data(c).get(r, daysback);
	}

	@Override
	public int dataDivider() {
		return 1;
	}

	@Override
	public INT_OE<Induvidual> indu() {
		return indu;
	}

	@Override
	public HISTORY_INT_OBJECT<Race> data() {
		return data(null);
	}

	@Override
	public HISTORY_INT_OBJECT<Race> data(HCLASS c) {
		return stats.data(c);
	}

	@Override
	public INT_O<Div> div() {
		return stats.div();
	}

	protected abstract int getDD(HCLASS s, Race r);
	
	@Override
	public void update(double ds) {
		
		for (int ci = 0; ci < HCLASS.ALL.size(); ci++) {
			HCLASS c = HCLASS.ALL.get(ci);

			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				int am = getDD(c, r);
				stats.inc(c, r, am - stats.data(c).get(r), -1);
			}
		}
		
	}

}