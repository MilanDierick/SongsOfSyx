package settlement.stats.colls;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.race.home.RaceHomeClass;
import init.resources.*;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.stats.*;
import settlement.stats.StatsInit.StatDisposable;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.equip.WearableResource;
import settlement.stats.stat.*;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
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
	public final StatHome GETTER;
	private final int[][] targets = new int[HCLASS.ALL.size()*RACES.all().size()][RESOURCES.ALL().size()];
	private final LIST<StatFurniture> currents;
	public final STAT materials;
	
	private final ArrayList<StatFurniture> tmp = new ArrayList<StatsHome.StatFurniture>(8);
	
	public StatsHome(StatsInit init){
		super(init, "HOME");
		GETTER = new StatHome(init);
		
		ArrayList<StatFurniture> cc = new ArrayList<>(8);
		while(cc.hasRoom()) {
			StatFurniture ss = new StatFurniture(cc.size(), init);
			cc.add(ss);
		}

		currents = cc;
		
		
		INT_OE<Induvidual> indu = new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				int am = 0;
				for (StatFurniture ss : currents)
					am += ss.current.indu().get(t);
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
		materials = new STATFacade("FURNITURE", init, indu) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				double am = 0;
				for (StatFurniture ss : currents)
					am += ss.current.data(s).get(r);
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
				if (r == null || r.home() == null)
					throw new RuntimeException(c + " "+r + " " + (r == null ? null : r.home()));
				RaceHomeClass cc = r.home().clas(c);
				if (cc == null)
					return STATS.POP().POP.data(c).get(r);
				return cc.amountTotal()*STATS.POP().POP.data(c).get(r);
			}
		};
		
		
		
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
		
		init.updatable.add(new StatUpdatableI() {
			
			@Override
			public void update16(Humanoid h, int updateR, boolean day, int updateI) {
				
				double wearRateI = rate(h.indu()) / 16.0;
				Induvidual i = h.indu();
				
				HOME home = GETTER.get(h.indu(), this);
				if (home != null) {
					wearRateI *= 1 + CLAMP.d((1-home.isolation())*2, 0, 1);
					home.done();
				}
				
				if (RND.rFloat() < wearRateI) {
					for (int ri = 0; ri < currents.size(); ri++) {
						currents.get(ri).update(i);	
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
	
	public LIST<StatFurniture> getTmp(Induvidual i){
		return getTmp(i.clas(), i.race());
	}
	
	public StatFurniture furniture(int i){
		return currents.get(i);
	}
	
	public LIST<StatFurniture> getTmp(HCLASS cl, Race ra){
		LIST<RES_AMOUNT> li = ra.home().clas(cl).resources();
		tmp.clearSloppy();
		for (int i = 0; i < li.size(); i++) {
			tmp.add(currents.get(i));
		}
		return tmp;
	}
	
	public double rate(Induvidual i) {
		return CLAMP.d(0.25/(BOOSTABLES.CIVICS().FURNITURE.get(i)), 0, 1);
	}
	
	public double rate(HCLASS cl, Race ra) {
		return CLAMP.d(0.25/(BOOSTABLES.CIVICS().FURNITURE.get(RACES.clP(ra, cl))), 0, 1);
	}
	
	public int current(Humanoid h, int rI) {
		return currents.get(rI).current.indu().get(h.indu());
	}
	
	public int current(HCLASS c, Race type, int resI) {
		if (type == null) {
			RES_AMOUNT ra = RACES.res().homeResMax(c).get(resI);
			int m = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				int i = 0;
				for (RES_AMOUNT rr : RACES.all().get(ri).home().clas(c).resources()) {
					
					if (rr.resource() == ra.resource()) {
						m += currents.get(i).current.data(c).get(RACES.all().get(ri));
					}
					i++;
				}
			}
			return m;
		}
		return currents.get(resI).current.data(c).get(type);
	}
	
	public int needed(HCLASS c, Race type, int resI) {
		if (type == null) {
			RES_AMOUNT ra = RACES.res().homeResMax(c).get(resI);
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
	
//	public boolean shouldFetch(Humanoid h, int ri) {
//		int tar = target(h, h.race().home().clas(h.indu().clas()).resources().get(ri).resource());
//		int c = current(h, ri);
//		if (c < tar)
//			return true;
//		return false;
//	}
	
//	public void fetchResource(Humanoid h, RESOURCE res) {
//		int ii = 0;
//		FACTIONS.player().res().inc(res, RTYPE.FURNISH, -1);
//		for (RES_AMOUNT aa : h.race().home().clas(h.indu().clas()).resources()) {
//			if (res == aa.resource()) {
//				int tar = target(h, h.race().home().clas(h.indu().clas()).resources().get(ii).resource());
//				int c = currents.get(ii).current.indu().get(h.indu());
//				if (c < tar)
//					currents.get(ii).current.indu().inc(h.indu(), 1);
//				return;
//			}
//			ii++;
//		}
//		GETTER.get(h, this).resUpdate().done();
//		
//	}
	
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
	
	public void dump(Humanoid a) {
		HOME home = STATS.HOME().GETTER.get(a, this);
		Induvidual i = a.indu();
		for (StatFurniture e : STATS.HOME().getTmp(i)) {
			int toDump = -e.needed(a.indu());
			if (toDump > 0) {
				e.inc(i, -toDump);
				if (home != null)
					SETT.THINGS().resources.create(home.service(), e.resource(i), toDump);
				else
					SETT.THINGS().resources.create(a.physics.tileC(), e.resource(i), toDump);
					
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
	
	public final class StatFurniture implements WearableResource {
		
		private final int resI;
		public final STATData current;
		private final INT_OE<Induvidual> counter;
		
		StatFurniture(int resI, StatsInit init){
			this.resI = resI;
			current = new STATData(null, init, init.count .new DataNibble());
			counter = init.count.new DataByte();
		}

		void update(Induvidual i) {
			int am = current.indu().get(i)-(counter.get(i)>>4);
			if (am > 0)
				counter.inc(i, am);
		}
		
		@Override
		public RESOURCE resource(Induvidual i) {
			LIST<RES_AMOUNT> li = i.race().home().clas(i.clas()).resources();
			if (resI >= li.size())
				return null;
			return li.get(resI).resource();
		}

		@Override
		public void wearOut(Induvidual i) {
			
			int c = counter.get(i);
			int am = c >> 4;
			c &= 0x0F;
			counter.set(i, c);
			if (am == 0)
				return;
			
			am = CLAMP.i(am, 0, current.indu().get(i));
			current.indu().inc(i, -am);
		}

		@Override
		public int max(Induvidual i) {
			LIST<RES_AMOUNT> li = i.race().home().clas(i.clas()).resources();
			if (resI >= li.size())
				return 0;
			return li.get(resI).amount();
		}

		@Override
		public int target(Induvidual i) {
			if (!GETTER.has(i))
				return 0;
			RESOURCE res = resource(i);
			if (res == null)
				return 0;
			return StatsHome.this.target(i.clas(), i.race(),res);
		}

		@Override
		public double wearPerYear(Induvidual i) {
			return CLAMP.d(0.5/(BOOSTABLES.CIVICS().FURNITURE.get(i)), 0, 1);
		}

		@Override
		public void set(Induvidual i, int am) {
			int old = current.indu().get(i);
			if (am != old) {
				
				current.indu().set(i, CLAMP.i(am, 0, max(i)));
				if (i.player() && i.added()) {
					FACTIONS.player().res().inc(resource(i), RTYPE.FURNISH, old-current.indu().get(i));
				}
			}
			
		}

		@Override
		public int needed(Induvidual i) {
			int am = target(i)-get(i) + (counter.get(i)>>4);
			if (am < 0) {
				wearOut(i);
				am = target(i)-get(i);
				if (am < 0) {
					int c = counter.get(i)&0x0F;
					if (RND.rInt(16) < c)
						current.indu().inc(i, -1);
					counter.set(i, 0);
					return target(i)-get(i);
				}
			}
			return am;
		}
		
		
		@Override
		public int get(Induvidual i) {
			return current.indu().get(i);
		}
		
	}
	
	public final static class StatHome implements StatDisposable{
		
		private final INT_OE<Induvidual> xx; 
		private final INT_OE<Induvidual> yy; 
		private final STATData stat;
		public final STAT hasSearched;
		public final INFO info;
		
		StatHome(StatsInit init){
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
			
			
			init.disposable.add(this);
			
			hasSearched = new STATData(null, init, init.count.new DataBit());

		}
		
		public boolean has(Humanoid h) {
			return (xx.get(h.indu()) != 0);
		}
		
		public boolean has(Induvidual h) {
			return (xx.get(h) != 0);
		}
		
		public HOME get(Induvidual f, Object user) {
			if (xx.get(f) == 0)
				return null;
			int tx = xx.get(f)-1;
			int ty = yy.get(f)-1;
			return HOME.get(tx, ty, user);
		}
		
		private Coo coo = new Coo();
		
		public COORDINATE hCoo(Humanoid f) {
			if (xx.get(f.indu()) == 0)
				return null;
			coo.set(xx.get(f.indu())-1, yy.get(f.indu())-1);
			return coo;
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
			if (h.isRemoved() || SETT.ENTITIES().getByID(h.id()) != h)
				throw new RuntimeException(h.isRemoved() + " " +  (SETT.ENTITIES().getByID(h.id()) != h));
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
				
			}else {
				xx.set(f, 0);
				yy.set(f, 0);
			}
			
			stat.addH(f);
			
		}
		
		public STAT stat() {
			return stat;
		}
		
	}
	
}
