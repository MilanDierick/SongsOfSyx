package settlement.stats;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import game.time.TIME;
import init.D;
import init.boostable.BOOSTABLES;
import init.race.RACES;
import init.race.Race;
import init.race.home.RaceHomeClass;
import init.resources.*;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.stats.Init.Disposable;
import settlement.stats.Init.Updatable;
import settlement.stats.STAT.STATData;
import settlement.stats.STAT.StatInfo;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import util.dic.DicMisc;
import util.info.INFO;

public class StatsHome extends StatCollection{
	
	private static CharSequence ¤¤desc = "¤This subject's place of residence.";
	static {
		D.ts(StatsHome.class);
	}
	private final LIST<STAT> all;
	public final StatHome GETTER;
	private final int[][] targets = new int[HCLASS.ALL.size()*RACES.all().size()][RESOURCES.ALL().size()];
	private final LIST<STAT.STATData> currents;
	private final LIST<INT_OE<Induvidual>> oneExtraRes;
	public final STAT materials;
	public final STAT space;
	
	StatsHome(Init init){
		super(init, "HOME");
		GETTER = new StatHome(init);
		space = GETTER.statSpace;
		INT_OE<Induvidual> indu = new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				int am = 0;
				for (STAT ss : currents)
					am += ss.indu().get(t);
				return am;
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				RaceHomeClass cc = t.race().home().clas(t.clas());
				int am = 0;
				for (RES_AMOUNT a : cc.resources())
					am += a.amount();
				return am;
			}

			@Override
			public void set(Induvidual t, int i) {
				// TODO Auto-generated method stub
				
			}
		
		};
		materials = new STAT.STATFacade("FURNITURE", init, indu) {
			
			@Override
			double getDD(HCLASS s, Race r, int daysBack) {
				double am = 0;
				for (STAT ss : currents)
					am += ss.data(s).get(r);
				double div = pdivider(s, r, daysBack);
				if (div == 0)
					return 0;
				return am/div;
			}
			
			@Override
			public int dataDivider() {
				return 1;
			}
			
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				if (r == null) {
					int am = 0;
					for (Race rr : RACES.all()) {
						am += pdivider(c, rr, daysback);
					}
					return am;
				}
				RaceHomeClass cc = r.home().clas(c);
				if (cc == null)
					return GETTER.stat().data(c).get(r, daysback);
				return cc.amountTotal()*GETTER.stat().data(c).get(r, daysback);
			}
		};
		
		ArrayList<STAT.STATData> cc = new ArrayList<>(8);
		ArrayList<INT_OE<Induvidual>> ee = new ArrayList<>(8);
		while(cc.hasRoom()) {
			STAT.STATData ss = new STAT.STATData(null, init, init.count .new DataNibble(), new StatInfo("", ""), null);
			ss.info().setMatters(false, false);
			cc.add(ss);
			ee.add(init.count.new DataBit());
		}

		currents = cc;
		oneExtraRes = ee;
		
		init.savables.add(new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				file.is(targets);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				file.is(targets);
			}
			
			@Override
			public void clear() {
				for (int[] t : targets)
					Arrays.fill(t, 0);
			}
		});
		
		all = makeStats(init);
		
		init.updatable.add(new Updatable() {
			
			private final double chanceI = 1.0/TIME.years().bitConversion(TIME.days());
			
			@Override
			public void update16(Humanoid h, int updateR, boolean day, int updateI) {
				
				if (day) {
					
					double chance = rate(h.indu())*chanceI;
					
					HOME home = GETTER.get(h.indu(), this);
					if (home != null) {
						chance *= 1 + CLAMP.d((1-home.isolation())*2, 0, 1);
						home.done();
					}
					
					
					
					for (int ri = 0; ri < currents.size(); ri++) {
						int am = currents.get(ri).indu().get(h.indu());
						if (chance*am > RND.rFloat()) {
							if (oneExtraRes.get(ri).get(h.indu()) == 1)
								oneExtraRes.get(ri).set(h.indu(), 0);
							else {
								currents.get(ri).indu().inc(h.indu(), -1);
							}
						}
						
					}
				}
				
				
			}
		});
		
//		new GAME_LOAD_FIXER() {
//			
//			@Override
//			protected void fix() {
//				new HomeFixer().fixAll();
//			}
//		};
	}
	
	public double rate(Induvidual i) {
		return CLAMP.d(0.5/(BOOSTABLES.CIVICS().FURNITURE.get(i)), 0, 1);
	}
	
	public double rate(HCLASS cl, Race ra) {
		return CLAMP.d(0.5/(BOOSTABLES.CIVICS().FURNITURE.get(cl, ra)), 0, 1);
	}
	
	public int current(Humanoid h, int rI) {
		return currents.get(rI).indu().get(h.indu());
	}
	
	public int current(HCLASS c, Race type, int resI) {
		if (type == null) {
			RES_AMOUNT ra = RACES.homeResMax(c).get(resI);
			int m = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				int i = 0;
				for (RES_AMOUNT rr : RACES.all().get(ri).home().clas(c).resources()) {
					
					if (rr.resource() == ra.resource()) {
						m += currents.get(i).data(c).get(RACES.all().get(ri));
					}
					i++;
				}
			}
			return m;
		}
		return currents.get(resI).data(c).get(type);
	}
	
	public int needed(HCLASS c, Race type, int resI) {
		if (type == null) {
			RES_AMOUNT ra = RACES.homeResMax(c).get(resI);
			int m = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				type = RACES.all().get(ri);
				m += type.home().clas(c).amount(ra.resource())*GETTER.stat().data(c).get(type);
			}
			return m;
		}
		return type.home().clas(c).resources().get(resI).amount()*GETTER.stat().data(c).get(type);
	}
	

	
	public int max(HCLASS c, Race type, RESOURCE res) {
		return type.home().clas(c).amount(res);
	}
	
	public int target(Humanoid h, RESOURCE res) {
		return target(h.indu().clas(), h.indu().race(), res);
	}
	
	public boolean shouldFetch(Humanoid h, int ri) {
		int tar = target(h, h.race().home().clas(h.indu().clas()).resources().get(ri).resource());
		int c = current(h, ri);
		if (c < tar)
			return true;
		if (tar > 0 && oneExtraRes.get(ri).get(h.indu()) == 0)
			return true;
		return false;
	}
	
	public void fetchResource(Humanoid h, RESOURCE res) {
		int ii = 0;
		FACTIONS.player().res().outHousehold.inc(res, 1);
		for (RES_AMOUNT aa : h.race().home().clas(h.indu().clas()).resources()) {
			if (res == aa.resource()) {
				int tar = target(h, h.race().home().clas(h.indu().clas()).resources().get(ii).resource());
				int c = currents.get(ii).indu().get(h.indu());
				if (c < tar)
					currents.get(ii).indu().inc(h.indu(), 1);
				else
					oneExtraRes.get(ii).set(h.indu(), 1);
				return;
			}
			ii++;
		}
		GETTER.get(h, this).resUpdate().done();
		
	}
	
	public int target(HCLASS c, Race type, RESOURCE res) {
		if (type == null) {
			int m = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				m = Math.max(m, target(c, r, res));
			}
			return m;
		}
		return CLAMP.i(targets[c.index()*RACES.all().size() + type.index][res.index()], 0, max(c, type, res));
	}
	
	public void dump(Humanoid h) {
		HOME home = GETTER.get(h, this);
		for (int i = 0; i < currents.size(); i++) {
			int am = currents.get(i).indu().get(h.indu()) + oneExtraRes.get(i).get(h.indu());
			currents.get(i).indu().set(h.indu(), 0);
			oneExtraRes.get(i).set(h.indu(), 0);
			if (am > 0) {
				RESOURCE res = h.race().home().clas(h.indu().clas()).resources().get(i).resource();
				if (home != null)
					SETT.THINGS().resources.create(home.service(), res, am);
				else
					SETT.THINGS().resources.create(h.tc().x(), h.tc().y(), res, am);
				FACTIONS.player().res().inDemolition.inc(res, am);
			}
			
		}
		if (home != null)
			home.done();
	}
	
	public void targetSet(int target, HCLASS c, Race type, RESOURCE res) {
		if (type == null) {
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				targetSet(target, c, r, res);
			}
			return;
		}
		target = CLAMP.i(target, 0, max(c, type, res));
		targets[c.index()*RACES.all().size() + type.index][res.index()] = target;
	}
	
	public final static class StatHome implements Disposable{
		
		private final INT_OE<Induvidual> xx; 
		private final INT_OE<Induvidual> yy; 
		private final STATData stat;
		private final STATData statSpace;
		public final STAT hasSearched;
		public final INFO info;
		
		StatHome(Init init){
			info = new INFO(DicMisc.¤¤Home, ¤¤desc);
			xx = init.count.new DataShort();
			yy = init.count.new DataShort();
			
			INT_OE<Induvidual> b = new INT_OE<Induvidual>(){

				@Override
				public int get(Induvidual t) {
					return xx.get(t) != 0 ? 1:0;
				}

				@Override
				public int min(Induvidual t) {
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return 1;
				}

				@Override
				public void set(Induvidual t, int i) {
					
				}
				
			};
			stat = new STATData("HOUSED", init, b);
			statSpace = new STATData("SPACE", init, init.count.new DataNibble()) {
				
				@Override
				public int pdivider(HCLASS c, Race r, int daysback) {
					return stat.data(c).get(r, daysback);
				}
			};
			
			
			init.disposable.add(this);
			
			hasSearched = new STAT.STATData(null, init, init.count.new DataBit(), new StatInfo("", "", ""), null);
			hasSearched.info().setMatters(false, true);
		}
		
		public boolean has(Humanoid h) {
			return (xx.get(h.indu()) != 0);
		}
		
		public HOME get(Induvidual f, Object user) {
			if (xx.get(f) == 0)
				return null;
			int tx = xx.get(f)-1;
			int ty = yy.get(f)-1;
			return HOME.get(tx, ty, user);
		}
		
		public HOME get(Humanoid h, Object user) {
			return get(h.indu(), user);
		}
		
		@Override
		public void dispose(Humanoid h) {
			STATS.HOME().dump(h);
			set(h, null);
		}
		
		public void set(Humanoid h, HOME home) {
			if (h.isRemoved() || h != SETT.ENTITIES().getByID(h.id()))
				throw new RuntimeException("removed!");
			
			Induvidual f = h.indu();
			hasSearched.indu().set(f, 0);
			stat.removeH(f);
			
			HOME ho = get(h.indu(), this);
			if (ho != null) {
				ho.vacate(h);
				ho.done();
			}
			
			if (home != null) {
				
				xx.set(f, home.service().x()+1);
				yy.set(f, home.service().y()+1);
				home.occupy(h);
				statSpace.indu().setD(f, home.space());
				
			}else {
				xx.set(f, 0);
				yy.set(f, 0);
				statSpace.indu().setD(f, 0);
			}
			
			stat.addH(f);
			
		}
		
		public STAT stat() {
			return stat;
		}
		
	}

	@Override
	public LIST<STAT> all() {
		return all;
	}
	
}
