package settlement.stats.colls;

import game.GAME;
import game.boosting.*;
import game.faction.Faction;
import game.faction.npc.NPCBonus;
import game.faction.npc.ruler.Royalty;
import init.D;
import init.need.NEEDS;
import init.race.*;
import init.religion.Religion;
import init.religion.Religions;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.room.spirit.temple.TempleInstance;
import settlement.stats.*;
import settlement.stats.StatsInit.*;
import settlement.stats.stat.*;
import settlement.stats.stat.SETT_STATISTICS.SettStatistics;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;
import util.dic.DicMisc;
import util.info.INFO;
import util.race.PERMISSION.Permission;
import world.regions.Region;

public class StatsReligion extends StatCollection {

	private static CharSequence ¤¤religion = "Religion";
	private static CharSequence ¤¤religionD = "Affiliated religion.";
	
	private static CharSequence ¤¤followers = "Followers";
	private static CharSequence ¤¤followersD = "The amount of followers for this religion.";
	
	private static CharSequence ¤¤access = "Temple access";
	private static CharSequence ¤¤accessD = "The access of the temples of this religion.";
	
	private static CharSequence ¤¤value = "Temple Respect";
	private static CharSequence ¤¤valueD = "The value of respect shown towards this religion. Comes from the temple designs and management of this belief";
	
	private static CharSequence ¤¤saccess = "Shrine access";
	private static CharSequence ¤¤saccessD = "The access of the shrines of this religion.";
	
	private static CharSequence ¤¤svalue = "Shrine Quality";
	private static CharSequence ¤¤svalueD = "The upgrade and degrade of your shrines.";
	
	static {
		D.ts(StatsReligion.class);
	}
	
	private final ArrayList<StatReligion> religions;
	public final LIST<StatReligion> ALL;
	public final GETTER_TRANSE<Induvidual, StatReligion> getter;
	
	public final STAT TEMPLE_TOTAL;
	public final STAT TEMPLE_ACCESS;
	public final STAT TEMPLE_QUALITY;
	
	public final STAT SHRINE_TOTAL;
	public final STAT SHRINE_ACCESS;
	public final STAT SHRINE_QUALITY;
	
	public final STAT OPPOSITION;
	
	public StatsReligion(StatsInit init){
		
		super(init, "RELIGION");
		
		religions = new ArrayList<>(Religions.ALL().size());
		
		
		for (Religion t : Religions.ALL()) {
			religions.add(new StatReligion(t, init));
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
		
		TEMPLE_TOTAL = new STATFacade("TEMPLE", init, indu) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				double a = TEMPLE_ACCESS.data(s).getD(r, daysBack);
				double q = TEMPLE_QUALITY.data(s).getD(r, daysBack);
				return a *(0.2 + 0.8*q);
			}
		};
		
		TEMPLE_ACCESS = new STATData(null, init, init.count.new DataBit() {
			@Override
			public void set(Induvidual t, int s) {
				
				if (t.added()) {
					StatReligion r = getter.get(t);
					r.temple_access.inc(t, -get(t));
					super.set(t, s);
					r.temple_access.inc(t, get(t));
					if (get(t) == 0) {
						TEMPLE_QUALITY.indu().set(t, 0);
					}
				}
				
				
			}
		}, new StatInfo(¤¤access, ¤¤access, ¤¤accessD));
		
		TEMPLE_QUALITY = new STATData(null, init, init.count.new DataNibble1() {
			@Override
			public void set(Induvidual t, int s) {
				if (t.added()) {
					if (TEMPLE_ACCESS.indu().get(t) == 0)
						s = 0;
					StatReligion r = getter.get(t);
					r.temple_quality.inc(t, -get(t));
					super.set(t, s);
					r.temple_quality.inc(t, get(t));
				}
			
			}
			
			
			
			
		}, new StatInfo(¤¤value, ¤¤value, ¤¤valueD)) {
			
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				return TEMPLE_ACCESS.data(c).get(r, daysback);
			}
		};
		
		SHRINE_TOTAL = new STATFacade("SHRINE", init, indu) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				double a = SHRINE_ACCESS.data(s).getD(r, daysBack);
				double q = SHRINE_QUALITY.data(s).getD(r, daysBack);
				return a *(0.2 + 0.8*q);
			}
		};
		
		SHRINE_ACCESS = new STATData(null, init, init.count.new DataBit() {
			@Override
			public void set(Induvidual t, int s) {
				StatReligion r = getter.get(t);
				r.shrine_access.inc(t, -get(t));
				super.set(t, s);
				r.shrine_access.inc(t, get(t));
				if (get(t) == 0) {
					SHRINE_QUALITY.indu().set(t, 0);
				}
			}
		}, new StatInfo(¤¤saccess, ¤¤saccess, ¤¤saccessD));
		
		SHRINE_QUALITY = new STATData(null, init, init.count.new DataNibble1() {
			@Override
			public void set(Induvidual t, int s) {
				if (SHRINE_ACCESS.indu().get(t) == 0)
					s = 0;
				StatReligion r = getter.get(t);
				r.temple_quality.inc(t, -get(t));
				super.set(t, s);
				r.temple_quality.inc(t, get(t));
			
			}
			
			
			
			
		}, new StatInfo(¤¤svalue, ¤¤svalue, ¤¤svalueD)) {
			
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				return SHRINE_ACCESS.data(c).get(r, daysback);
			}
		};
		
		OPPOSITION = new STATImp("RELIGION_OPPOSITION", init) {
			
			@Override
			protected int getDD(HCLASS s, Race r) {
				return (int) (opposition()*pdivider(s, r, 0));
			}
		};
		OPPOSITION.info().setMatters(true, false);
		
		
		init.updatable.add(new StatUpdatableI() {
			
			@Override
			public void update16(Humanoid h, int updateR, boolean day, int updateI) {
				if (day) {
					double f = TEMPLE_TOTAL.indu().getD(h.indu());
					if (f <= 0) {
						for (StatReligion r : ALL) {
							double d = r.temple_access.data().getD(null)*r.temple_quality.data().getD(null);
							d *= Math.min(RACES.boosts().religion(h.race(), r.religion), 0.05);
							if (RND.rFloat() < d) {
								getter.set(h.indu(), r);
								return;
							}
						}
					}
				}
				
			}
		});
		
		BOOSTING.connecter(new ACTION() {
			
			@Override
			public void exe() {
				
				KeyMap<BB> map = new KeyMap<>();
				
				for (int ri = 0; ri < Religions.ALL().size(); ri++) {
					Religion r = Religions.ALL().get(ri);
					for (int bi = 0; bi < r.bsett.all().size(); bi++) {
						BoostSpec s = r.bsett.all().get(bi);
						String k = s.identifier();
						if (!map.containsKey(k)) {
							BB b = new BB(s.boostable, s.booster.isMul);
							TEMPLE_TOTAL.boosters.push(b, s.boostable);
							map.put(k, b);
						}
					}
					
				}
				
				
			}
		});
		
	}
	
	public void setChildReligion(Humanoid h) {
		
		Race r = h.race();
		double f = RND.rFloat();
		for (StatReligion rel : ALL) {
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
			StatReligion r = religions.get(ri);
			for (int ri2 = 0; ri2 < religions.size(); ri2++) {
				StatReligion r2 = religions.get(ri2);
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
		
		NEEDS.TYPES().TEMPLE.stat().fixMax(h.indu());
	}
	
	public void setAccess(Humanoid h) {
		
		Room r = SETT.ROOMS().map.get(h.tc());
		if (r != null && r.blueprint() instanceof ROOM_TEMPLE) {
			TempleInstance t = (TempleInstance) r;
			TEMPLE_ACCESS.indu().set(h.indu(), 1);
			TEMPLE_QUALITY.indu().setD(h.indu(), t.quality());
		}
		NEEDS.TYPES().TEMPLE.stat().fixMax(h.indu());
		
		
		
	}
	
	
	private final class Getter implements GETTER_TRANSE<Induvidual, StatReligion>, Addable, StatInitable{

		private final INT_OE<Induvidual> ii;
		private final INFO info = new INFO(¤¤religion, ¤¤religionD);
		
		Getter(StatsInit init){
			ii = init.count. new DataByte();
			init.addable.add(this);
			init.initable.add(this);
		}
		
		@Override
		public StatReligion get(Induvidual f) {
			return religions.get(ii.get(f));
		}

		@Override
		public void set(Induvidual f, StatReligion t) {
			TEMPLE_ACCESS.indu().set(f, 0);
			TEMPLE_QUALITY.indu().set(f, 0);
			removeH(f);
			ii.set(f, t.index());
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
			for (StatReligion re : ALL) {
				d += RACES.boosts().religion(h.race(), re.religion);
				if (d >= r) {
					getter.set(h, re);
					return;
				}
			}
			getter.set(h, ALL.get(ALL.size()-1));
		}

		@Override
		public void addPrivate(Induvidual i) {
			StatReligion r = get(i);
			r.followers.inc(i, 1);
			r.temple_access.inc(i, TEMPLE_ACCESS.indu().get(i));
			r.temple_quality.inc(i, TEMPLE_QUALITY.indu().get(i));
			
		}

		@Override
		public void removePrivate(Induvidual i) {
			StatReligion r = get(i);
			r.followers.inc(i, -1);
			r.temple_access.inc(i, -TEMPLE_ACCESS.indu().get(i));
			r.temple_quality.inc(i, -TEMPLE_QUALITY.indu().get(i));
		}
		
	}
	

	
	public final class StatReligion implements INDEXED{
		
		public final Religion religion;
		
		public final INFO info;
		public final SettStatistics followers;
		public final SettStatistics temple_access;
		public final SettStatistics temple_quality;
		public final SettStatistics shrine_access;
		public final SettStatistics shrine_quality;
		public final Permission permission = new Permission();
		
		
		StatReligion(Religion religion, StatsInit init){
			this.religion = religion;
			
			

			info = religion.info;
			permission.setDef(true);
			
			followers = new SettStatistics(init, ¤¤followers, ¤¤followersD);
			temple_access = new SettStatistics(init, ¤¤access, ¤¤accessD) {
				@Override
				protected int popDivider(HCLASS c, Race r, int daysback) {
					return followers.data(c).get(r, daysback);
				}
			};
			temple_quality = new SettStatistics(init, ¤¤value, ¤¤valueD) {
				@Override
				protected int popDivider(HCLASS c, Race r, int daysback) {
					return temple_access.data(c).get(r, daysback);
				}
				
				@Override
				public int dataDivider() {
					return TEMPLE_QUALITY.indu().max(null);
				}
			};
			shrine_access = new SettStatistics(init, ¤¤access, ¤¤accessD) {
				@Override
				protected int popDivider(HCLASS c, Race r, int daysback) {
					return followers.data(c).get(r, daysback);
				}
			};
			shrine_quality = new SettStatistics(init, ¤¤value, ¤¤valueD) {
				@Override
				protected int popDivider(HCLASS c, Race r, int daysback) {
					return shrine_access.data(c).get(r, daysback);
				}
				
				@Override
				public int dataDivider() {
					return SHRINE_QUALITY.indu().max(null);
				}
			};
			for (HCLASS s : HCLASS.ALL)
				permission.set(s, null, true);
			init.savables.add(permission);

			
		}
		
		public double opposition(StatReligion other) {
			return religion.opposition(other.religion);
		}

		@Override
		public int index() {
			return religion.index();
		}
		
	}
	
	private class BB extends Booster {

		private final Booster[] vv = new Booster[Religions.ALL().size()];
		private final double min;
		private final double max;
		private final double aa;
		
		public BB(Boostable bb, boolean isMul) {
			super(new BSourceInfo(DicMisc.¤¤Religion, UI.icons().s.star), isMul);
			
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			
//			if (isMul)
//				Arrays.fill(vv, 1.0);
//			
			aa = isMul ? 1 : 0;
			
			for (Religion r : Religions.ALL()) {
				for (BoostSpec s : r.bsett.all()) {
					if (s.boostable == bb && s.booster.isMul == isMul) {
						double v = isMul ? s.booster.getValue(1.0)-aa : s.booster.getValue(1.0);
						vv[r.index()] = s.booster;
						
						min = Math.min(min, s.booster.from());
						max = Math.max(max, s.booster.to());
					}
				}
			}
			
			this.min = min;
			this.max = max;
			
		}

		@Override
		public double get(Boostable bo, BOOSTABLE_O o) {
			return o.boostableValue(bo, this);
		}
		
		@Override
		public double vGet(Region reg) {
			return 0;
		}

		@Override
		public double vGet(Induvidual indu) {
			return vv(STATS.RELIGION().getter.get(indu).religion, STATS.RELIGION().TEMPLE_TOTAL.indu().getD(indu));
		}

		private double vv(Religion rel, double v) {
			if (vv[rel.index()] == null)
				return aa;
			return vv[rel.index()].getValue(v);
		}
		
		@Override
		public double vGet(Div div) {
			double dd = 0;
			for (int ri = 0; ri < Religions.ALL().size(); ri++) {
				StatReligion rl = STATS.RELIGION().ALL.get(ri);
				double v = rl.temple_access.div().getD(div)*rl.temple_quality.div().getD(div);
				dd += vv(rl.religion, v)*rl.followers.div().getD(div);
				
			}
			return dd;
		}

		@Override
		public double vGet(Faction f) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double vGet(POP_CL reg) {
			return vGet(reg, 0);
		}
		
		@Override
		public double vGet(POP_CL reg, int daysBack) {
			double dd = 0;
			for (int ri = 0; ri < Religions.ALL().size(); ri++) {
				StatReligion rl = STATS.RELIGION().ALL.get(ri);
				double v = rl.temple_access.data(reg.cl).getD(reg.race, daysBack)*rl.temple_quality.data(reg.cl).getD(reg.race, daysBack);
				dd += vv(rl.religion, v)*rl.followers.data(reg.cl).getD(reg.race, daysBack);
			}
			return dd;
		}

		@Override
		public double vGet(Royalty roy) {
			return vv(STATS.RELIGION().getter.get(roy.induvidual).religion,roy.court.faction.bonus.get(STATS.RELIGION().TEMPLE_TOTAL.index()) );
			//return aa + (vv[STATS.RELIGION().getter.get(roy.induvidual).index()]-aa)*roy.court.faction.bonus.get(STATS.RELIGION().TEMPLE_TOTAL.index());
		}

		@Override
		public double vGet(Race race) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double vGet(NPCBonus bonus) {
			return vGet(bonus.faction.court().king().roy().induvidual);
		}

		@Override
		public boolean has(Class<? extends BOOSTABLE_O> b) {
			return b == Induvidual.class || b == Div.class ||b == POP_CL.class || b == NPCBonus.class;
		}

		@Override
		public double from() {
			return min;
		}

		@Override
		public double to() {
			return max;
		}
		
		
	}



}
