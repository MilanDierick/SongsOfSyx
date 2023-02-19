package settlement.stats;

import game.time.TIME;
import game.time.TIMECYCLE;
import init.race.*;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.Init.Addable;
import settlement.stats.Init.Updatable2;
import settlement.stats.STANDING.StandingDef;
import settlement.stats.StatsBoosts.StatBooster;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.statistics.HISTORY_INT.HISTORY_INT_OBJECT;

public abstract class STAT implements INDEXED, SETT_STATISTICS {

	private StatDecree decree;

	LinkedList<StatBooster> boosts = new LinkedList<>();
	
	abstract public INT_OE<Induvidual> indu();

	@Override
	abstract public StatInfo info();

	abstract public STANDING standing();

	abstract public String key();

	public int pdivider(HCLASS c, Race r, int daysback) {
		return STATS.POP().POP.data(c).get(r, daysback);
	}

	void addDecree(StatDecree d) {
		this.decree = d;
	}

	public StatDecree decree() {
		return decree;
	}

	public boolean hasIndu() {
		return false;
	}
	
	public LIST<StatBooster> boosts(){
		return boosts;
	}

	static class STATData extends STAT implements Addable {

		private final int index;
		public final SettStatistics stats;
		private final INT_OE<Induvidual> indu;
		private final STANDING happiness;
		private final StatInfo info;
		private final String key;

		STATData(String key, Init init, INT_OE<Induvidual> data, StatInfo info, StandingDef def) {
			index = init.stats.add(this);
			init.addable.add(this);
			this.key = key;

			if (key != null && init.jText.has(key))
				info = new StatInfo(init.jText.json(key));
			
			this.info = info;
			
			stats = new SettStatistics(init, info) {

				@Override
				protected int popDivider(HCLASS c, Race r, int daysback) {
					return (int) pdivider(c, r, daysback);
				}

				@Override
				public int dataDivider() {
					return data.max(null);
				}
			};

			happiness = new STANDING(this, def);

			indu = new INT_OE<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return data.get(t);
				}

				@Override
				public int min(Induvidual t) {
					return data.min(t);
				}

				@Override
				public int max(Induvidual t) {
					return data.max(t);
				}

				@Override
				public void set(Induvidual t, int i) {
					removeH(t);
					data.set(t, i);
					addH(t);
				}

			};

		}

		STATData(String key, Init init, INT_OE<Induvidual> data) {
			this(key, init, data, null, new StandingDef(init.jData.json(key), 0));
			if (data.max(null) == 1) {
				info.setInt();
			}
		}

		@Override
		public int pdivider(HCLASS c, Race r, int daysback) {
			return STATS.POP().POP.data(c).get(r, daysback);
		}

		@Override
		public int dataDivider() {
			return indu.max(null);
		}

		@Override
		public void addH(Induvidual i) {
			stats.inc(i, indu.get(i));
		}

		@Override
		public void removeH(Induvidual i) {
			stats.inc(i, -indu.get(i));
		}

		@Override
		public INT_OE<Induvidual> indu() {
			return indu;
		}

		@Override
		public StatInfo info() {
			return info;
		}

		@Override
		public STANDING standing() {
			return happiness;
		}

		@Override
		public int index() {
			return index;
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

		@Override
		public String key() {
			return key;
		}

		@Override
		public boolean hasIndu() {
			return key != null && key.length() > 0;
		}

	}

	static abstract class STATImp extends STAT implements Updatable2 {

		private final int index;

		private final SettStatistics stats;
		private final INT_OE<Induvidual> indu;
		private final STANDING happiness;
		private final StatInfo info;
		private final String key;

		STATImp(String key, Init init) {
			this(key, init, null, new StandingDef(init.jData.json(key), 0));
		}
		
		STATImp(String key, Init init, double defaultInput) {
			this(key, init, null, new StandingDef(init.jData.json(key), defaultInput));
		}

//		STATImp(String key, Init init, CharSequence name, CharSequence desc, StandingDef def) {
//			this(key, init, new StatInfo(name, desc), def);
//		}
//
//		STATImp(String key, Init init, CharSequence name, CharSequence names, CharSequence desc, StandingDef def) {
//			this(key, init, new StatInfo(name, names, desc), def);
//		}

		STATImp(String key, Init init, StatInfo info, StandingDef def) {
			index = init.stats.add(this);
			this.key = key;

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

			happiness = new STANDING(this, def);
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
			if (key != null && init.jText.has(key))
				info = new StatInfo(init.jText.json(key));
			this.info = info;
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
		public StatInfo info() {
			return info;
		}

		@Override
		public STANDING standing() {
			return happiness;
		}

		@Override
		public int index() {
			return index;
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

		abstract int getDD(HCLASS s, Race r);
		
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

		@Override
		public String key() {
			return key;
		}

	}

	static abstract class STATFacade extends STAT {

		private final int index;
		private final ArrayList<HISTORY_INT_OBJECT<Race>> datas = new ArrayList<HISTORY_INT_OBJECT<Race>>(
				HCLASS.ALL().size() + 1);
		private final INT_O<Div> div;
		private final INT_OE<Induvidual> indu;
		private final STANDING happiness;
		private final StatInfo info;
		private final String key;

		STATFacade(String key, Init init, INT_OE<Induvidual> indu) {
			this(key, init, null, new StandingDef(init.jData.json(key), 0), indu);
		}
		
		STATFacade(String key, Init init) {
			this(key, init, null, new StandingDef(init.jData.json(key), 0));
		}

		STATFacade(String key, Init init, double def) {
			this(key, init, null, new StandingDef(init.jData.json(key), def));
		}

//		STATFacade(String key, Init init, CharSequence name, CharSequence desc, StandingDef def) {
//			this(key, init, new StatInfo(name, desc), def);
//		}

		STATFacade(String key, Init init, StatInfo info, StandingDef def) {
			this(key, init, info, def, null);
		}

		STATFacade(String key, Init init, StatInfo info, StandingDef def, INT_OE<Induvidual> indu) {
			index = init.stats.add(this);
			this.key = key;
			for (HCLASS c : HCLASS.ALL()) {

				datas.add(new HISTORY_INT_OBJECT<Race>() {

					@Override
					public int min(Race t) {
						return 0;
					}

					@Override
					public int max(Race t) {
						return dataDivider() * pdivider(c, t, 0);
					}

					@Override
					public double getD(Race t, int fromZero) {

						return getDD(c, t, fromZero);
					}

					@Override
					public TIMECYCLE time() {
						return TIME.days();
					}

					@Override
					public int historyRecords() {
						return STATS.DAYS_SAVED;
					}

					@Override
					public int get(Race t, int fromZero) {
						double d = dataDivider() * pdivider(c, t, 0);
						return (int) (getDD(c, t, fromZero) * d);
					}

				});
			}
			happiness = new STANDING(this, def);
			div = new INT_O<Div>() {

				@Override
				public int get(Div t) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public int min(Div t) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public int max(Div t) {
					// TODO Auto-generated method stub
					return 0;
				}

			};
			if (indu == null)
				indu = new INT_OE<Induvidual>() {

					@Override
					public int get(Induvidual t) {
						return (int) (64 * getDD(t.clas(), t.race(), 0));
					}

					@Override
					public int min(Induvidual t) {
						return 0;
					}

					@Override
					public int max(Induvidual t) {
						return 64;
					}

					@Override
					public void set(Induvidual t, int i) {

					}

				};
			this.indu = indu;
			if (key != null && init.jText.has(key))
				info = new StatInfo(init.jText.json(key));
			this.info = info;
		}

		@Override
		public INT_OE<Induvidual> indu() {
			return indu;
		}

		@Override
		public StatInfo info() {
			return info;
		}

		@Override
		public STANDING standing() {
			return happiness;
		}

		@Override
		public int index() {
			return index;
		}

		@Override
		public HISTORY_INT_OBJECT<Race> data() {
			return data(null);
		}

		@Override
		public HISTORY_INT_OBJECT<Race> data(HCLASS c) {
			if (c == null)
				return datas.get(datas.size() - 1);
			return datas.get(c.index());
		}

		@Override
		public INT_O<Div> div() {
			return div;
		}

		abstract double getDD(HCLASS s, Race r, int daysBack);

		@Override
		public int pdivider(HCLASS c, Race r, int daysback) {
			return STATS.POP().POP.data(c).get(r, daysback);
		}

		@Override
		public int dataDivider() {
			return 1;
		}

		@Override
		public String key() {
			return key;
		}

	}

	public static class StatInfo extends INFO {

		private boolean isInt = false;
		private boolean matters = true;
		private boolean hasIndu = true;
		public Opinion defOpinion = Opinion.DEF;

		StatInfo(Json json) {
			super(json);
			defOpinion = new Opinion(json, "_OPINION");
		}

		StatInfo(CharSequence name, CharSequence desc) {
			super(name, desc);
		}

		StatInfo(CharSequence name, CharSequence names, CharSequence desc) {
			super(name, names, desc, null);
		}

		public boolean isInt() {
			return isInt;
		}

		void setInt() {
			isInt = true;
		}
		
		public void setOpinion(CharSequence more, CharSequence desc) {
			defOpinion = new Opinion().setMore(more).setLess(more);
		}
		
		void setMatters(boolean matters, boolean hasIndu) {
			this.matters = matters;
			this.hasIndu = hasIndu;
		}
		
		public boolean indu() {
			return hasIndu;
		}
		
		public boolean matters() {
			return matters;
		}

	}

}
