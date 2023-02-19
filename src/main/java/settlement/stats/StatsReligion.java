package settlement.stats;

import java.util.Arrays;

import game.GAME;
import init.D;
import init.boostable.BBoost;
import init.boostable.BOOSTABLES;
import init.paths.PATHS;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.room.spirit.temple.TempleInstance;
import settlement.stats.Init.*;
import settlement.stats.SETT_STATISTICS.SettStatistics;
import settlement.stats.STAT.StatInfo;
import settlement.stats.StatsBoosts.StatBooster;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.race.PERMISSION.Permission;

public class StatsReligion extends StatCollection {

	private final ArrayList<Religion> religions;
	public final LIST<Religion> ALL;
	private final LIST<STAT> all;
	public final GETTER_TRANSE<Induvidual, Religion> getter;
	
	public final STAT TEMPLE_TOTAL;
	public final STAT TEMPLE_ACCESS;
	public final STAT TEMPLE_QUALITY;
	public final STAT OPPOSITION;
	
	StatsReligion(Init init){
		
		super(init, "RELIGION");
		
		LIST<ROOM_TEMPLE> ts = SETT.ROOMS().TEMPLES;
		religions = new ArrayList<>(ts.size());
		
		
		for (ROOM_TEMPLE t : ts) {
			religions.add(new Religion(t, init));
		}
		
		ALL = religions;
		
		getter = new Getter(init);
		
		INT_OE<Induvidual> indu = new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				return (int) (TEMPLE_ACCESS.indu().get(t)*TEMPLE_QUALITY.indu().get(t)); 
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				return TEMPLE_QUALITY.indu().max(t);
			}

			@Override
			public void set(Induvidual t, int i) {
				
			}
		
		};
		
		TEMPLE_TOTAL = new STAT.STATFacade("FAITH", init, indu) {
			
			@Override
			double getDD(HCLASS s, Race r, int daysBack) {
				double a = TEMPLE_ACCESS.data(s).getD(r, daysBack);
				double q = TEMPLE_QUALITY.data(s).getD(r, daysBack);
				return a *(0.2 + 0.8*q);
			}
		};
		
		TEMPLE_ACCESS = new STAT.STATData(null, init, init.count.new DataBit() {
			@Override
			public void set(Induvidual t, int s) {
				Religion r = getter.get(t);
				r.temple_access.inc(t, -get(t));
				super.set(t, s);
				r.temple_access.inc(t, get(t));
				if (get(t) == 0) {
					TEMPLE_QUALITY.indu().set(t, 0);
				}
			}
		}, new StatInfo(¤¤access, ¤¤access, ¤¤accessD), null);
		
		TEMPLE_QUALITY = new STAT.STATData(null, init, init.count.new DataNibble1() {
			@Override
			public void set(Induvidual t, int s) {
				if (TEMPLE_ACCESS.indu().get(t) == 0)
					s = 0;
				Religion r = getter.get(t);
				r.temple_value.inc(t, -get(t));
				super.set(t, s);
				r.temple_value.inc(t, get(t));
			
			}
			
			
			
			
		}, new StatInfo(¤¤value, ¤¤value, ¤¤valueD), null) {
			
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				return TEMPLE_ACCESS.data(c).get(r, daysback);
			}
		};
		
		OPPOSITION = new STAT.STATImp("RELIGION_OPPOSITION", init) {
			
			@Override
			int getDD(HCLASS s, Race r) {
				return (int) (opposition()*pdivider(s, r, 0));
			}
		};
		OPPOSITION.info().setMatters(true, false);
		
		
		init.updatable.add(new Updatable() {
			
			@Override
			public void update16(Humanoid h, int updateR, boolean day, int updateI) {
				if (day) {
					double f = TEMPLE_TOTAL.indu().getD(h.indu());
					if (f <= 0) {
						for (Religion r : ALL) {
							double d = r.temple_access.data().getD(null)*r.temple_value.data().getD(null);
							d *= Math.min(h.race().stats().religion(r), 0.05);
							if (RND.rFloat() < d) {
								getter.set(h.indu(), r);
								return;
							}
						}
					}
				}
				
			}
		});
		
		this.all = makeStats(init);
	}
	
	public void setChildReligion(Humanoid h) {
		
		Race r = h.race();
		double f = RND.rFloat();
		for (Religion rel : ALL) {
			f -= rel.followers.data(h.indu().clas()).getD(r);
			if (f <= 0) {
				getter.set(h.indu(), rel);
				return;
			}
		}
	}
	
	private double opCache = 0;
	private int updateI = -1;
	
	private double opposition() {
		if (updateI == GAME.updateI())
			return opCache;
		
		double pop = STATS.POP().POP.data().get(null);
		if (pop == 0)
			return 1;
		double v = 0;
		for (int ri = 0; ri < religions.size(); ri++) {
			double vv = 0;
			Religion r = religions.get(ri);
			for (int ri2 = 0; ri2 < religions.size(); ri2++) {
				Religion r2 = religions.get(ri2);
				double am = r2.followers.data().get(null)/pop;
				am *= r.opposition(r2);
				vv += am;
			}
			v += vv*r.followers.data().get(null);
		}
		v /= pop;
		opCache = CLAMP.d(v, 0, 1);
		updateI = GAME.updateI();
		return opCache;
	}
	
	public void clearAccess(Humanoid h) {
		TEMPLE_ACCESS.indu().set(h.indu(), 0);
		TEMPLE_QUALITY.indu().set(h.indu(), 0);
		STATS.NEEDS().RELIGION.fixMax(h.indu());
	}
	
	public void setAccess(Humanoid h) {
		
		Room r = SETT.ROOMS().map.get(h.tc());
		if (r != null && r.blueprint() instanceof ROOM_TEMPLE) {
			TempleInstance t = (TempleInstance) r;
			TEMPLE_ACCESS.indu().set(h.indu(), 1);
			TEMPLE_QUALITY.indu().setD(h.indu(), t.quality());
		}
		STATS.NEEDS().RELIGION.fixMax(h.indu());
		
		
		
	}
	
	
	private final class Getter implements GETTER_TRANSE<Induvidual, Religion>, Addable, Initable{

		private final INT_OE<Induvidual> ii;
		private final INFO info = new INFO(¤¤religion, ¤¤religionD);
		
		Getter(Init init){
			ii = init.count. new DataByte();
			init.addable.add(this);
			init.initable.add(this);
		}
		
		@Override
		public Religion get(Induvidual f) {
			return religions.get(ii.get(f));
		}

		@Override
		public void set(Induvidual f, Religion t) {
			TEMPLE_ACCESS.indu().set(f, 0);
			TEMPLE_QUALITY.indu().set(f, 0);
			removeH(f);
			ii.set(f, t.temple.typeIndex());
			addH(f);
		}
		
		@Override
		public INFO info() {
			return info;
		}

		@Override
		public void init(Induvidual h) {
			double r = RND.rFloat();
			double d = 0;
			for (Religion re : ALL) {
				d += h.race().stats().religion(re);
				if (d >= r) {
					getter.set(h, re);
					return;
				}
			}
			getter.set(h, ALL.get(ALL.size()-1));
		}

		@Override
		public void addH(Induvidual i) {
			Religion r = get(i);
			r.followers.inc(i, 1);
			r.temple_access.inc(i, TEMPLE_ACCESS.indu().get(i));
			r.temple_value.inc(i, TEMPLE_QUALITY.indu().get(i));
			
		}

		@Override
		public void removeH(Induvidual i) {
			Religion r = get(i);
			r.followers.inc(i, -1);
			r.temple_access.inc(i, -TEMPLE_ACCESS.indu().get(i));
			r.temple_value.inc(i, -TEMPLE_QUALITY.indu().get(i));
		}
		
	}
	
	private static CharSequence ¤¤religion = "Religion";
	private static CharSequence ¤¤religionD = "Affiliated religion.";
	
	private static CharSequence ¤¤followers = "Followers";
	private static CharSequence ¤¤followersD = "The amount of followers for this religion.";
	
	private static CharSequence ¤¤access = "Temple access";
	private static CharSequence ¤¤accessD = "The access of the temples of this religion.";
	
	private static CharSequence ¤¤value = "Temple Respect";
	private static CharSequence ¤¤valueD = "The value of respect shown towards this religion. Comes from the temple designs and management of this belief";
	
	static {
		D.ts(StatsReligion.class);
	}
	
	public final class Religion implements INDEXED{
		
		public final ROOM_TEMPLE temple;
		public final COLOR color;
		public final INFO info;
		private final double[] liking;
		public final LIST<StatBooster> bonuses;

		public final double inclination;
		
		public final SettStatistics followers;
		public final SettStatistics temple_access;
		public final SettStatistics temple_value;
		public final Permission permission = new Permission();
		
		Religion(ROOM_TEMPLE temple, Init init){
			this.temple = temple;
			Json d = new Json(PATHS.INIT().getFolder("room").get(temple.key)).json("RELIGION");
			Json t = new Json(PATHS.TEXT().getFolder("room").get(temple.key)).json("RELIGION");
			info = new INFO(t);
			color = new ColorImp(d);
			LIST<BBoost> bonuses = BOOSTABLES.boosts(d);
			
			inclination = d.d("RACE_DEFAULT_INCLINATION");
			liking = new double[SETT.ROOMS().TEMPLES.size()];
			Arrays.fill(liking, 0);
			permission.setDef(true);
			
			new RoomsJson("OPPOSITION", d) {
				
				@Override
				public void doWithTheJson(RoomBlueprintImp room, Json j, String key) {
					
					if (room instanceof ROOM_TEMPLE) {
						
						liking[((ROOM_TEMPLE)room).typeIndex()] = j.d(room.key, 0, 1000);
					}
				}
			};
			
			followers = new SettStatistics(init, ¤¤followers, ¤¤followersD);
			temple_access = new SettStatistics(init, ¤¤access, ¤¤accessD) {
				@Override
				protected int popDivider(HCLASS c, Race r, int daysback) {
					return followers.data(c).get(r, daysback);
				}
			};
			temple_value = new SettStatistics(init, ¤¤value, ¤¤valueD) {
				@Override
				protected int popDivider(HCLASS c, Race r, int daysback) {
					return temple_access.data(c).get(r, daysback);
				}
				
				@Override
				public int dataDivider() {
					return TEMPLE_QUALITY.indu().max(null);
				}
			};
			for (HCLASS s : HCLASS.ALL)
				permission.set(s, null, true);
			init.savables.add(permission);
			ArrayList<StatBooster> bos = new ArrayList<>(bonuses.size());
			for (BBoost b : bonuses) {
				bos.add(new Boo(init, this, b));
			}
			this.bonuses = bos;
		}
		
		public double opposition(Religion other) {
			return liking[other.temple.typeIndex()];
		}

		@Override
		public int index() {
			return temple.typeIndex();
		}
		
	}
	
	private static class Boo extends StatBooster {

		private final Religion rl;
		
		Boo(Init init, Religion rl, BBoost boost) {
			super(init, ¤¤religion + ": " + rl.info.name, boost);
			this.rl = rl;
		}

		@Override
		public double pvalue(Induvidual v) {
			if (STATS.RELIGION().getter.get(v) == rl) {
				STATS.RELIGION().TEMPLE_TOTAL.indu().getD(v);
				return STATS.RELIGION().TEMPLE_TOTAL.indu().getD(v);
			}
			return 0;
		}

		@Override
		public double pvalue(HCLASS c, Race r) {
			return rl.followers.data(c).getD(r)*rl.temple_access.data(c).getD(r)*rl.temple_value.data(c).getD(r);
		}

		@Override
		public double pvalue(Div v) {
			return rl.followers.div().getD(v)*rl.temple_access.div().getD(v)*rl.temple_value.div().getD(v);
		}
		
		
	}

	@Override
	public LIST<STAT> all() {
		return all;
	}



}
