package world.map.regions;



import game.faction.FACTIONS;
import game.faction.Faction;
import init.RES;
import init.race.RACES;
import init.race.Race;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.info.INFO;
import world.World;
import world.army.*;
import world.army.WINDU.WDivGeneration;
import world.army.WINDU.WInduStored;
import world.entity.army.WArmy;
import world.map.regions.REGIOND.RResource;
import world.map.regions.RegionDecree.RegionDecreeImp;

public final class RegionMilitary extends RResource {
	
	
	public final RegionDecree decreeSoldiers;
	public final INT_OE<Region> soldiers;
	public final INT_O<Region> power;
	public final RegionFactors soldiers_target;
	
	RegionMilitary(RegionInit init) {
		
		decreeSoldiers = new RegionDecreeImp(init.count. new DataNibble(6), 2,
				DicArmy.¤¤Garrison,
				DicArmy.¤¤GarrisonD) {
			
		};
		
		INT_O<Region> power = new INT_O<Region>() {
		
			private final INFO info = new INFO(DicArmy.¤¤Garrison, 
				DicArmy.¤¤GarrisonD);
			
			@Override
			public int max(Region t) {
				return Integer.MAX_VALUE;
			};
			
			@Override
			public int get(Region t) {
				int p = 0;
				for (WDIV d : divisions(t))
					p += d.provess();
				return p;
			}

			@Override
			public int min(Region t) {
				return 0;
			}
			
			@Override
			public INFO info() {
				return info;
			}
			
		};
		
		this.power = power;
		
		soldiers = init.count. new DataShort(
				DicArmy.¤¤Garrison, 
				DicArmy.¤¤GarrisonD
				) {
			
			@Override
			public int get(Region t) {
				if (FACTIONS.player().capitolRegion() == t) {
					int pow = 0;
					for (WDIV d : World.ARMIES().playerGarrison()) {
						pow += d.men();
					}
					return pow;
				}
				return super.get(t);
			}
			
			@Override
			public int max(Region t) {
				return RES.config().BATTLE.REGION_MAX_MEN;
			}
			
		};
		
		new RegionFactor.RegionFactorImp(init.tmpLoyalty, soldiers.info()) {
			
			@Override
			public double getD(Region t) {
				double i = (int) Math.ceil(RES.config().BATTLE.REGION_MAX_MEN*REGIOND.POP().popValue(t));
				i = CLAMP.d(i, 0, RES.config().BATTLE.REGION_MAX_MEN);
				if (i == 0)
					return 1;
				
				return 1 + 0.5*soldiers.get(t)/(i);
			}
			
			@Override
			public double next(Region r) {
				double i = (int) Math.ceil(RES.config().BATTLE.REGION_MAX_MEN*REGIOND.POP().popValue(r));
				i = CLAMP.d(i, 0, RES.config().BATTLE.REGION_MAX_MEN);
				if (i == 0)
					return 1;
				return 1 + 0.5*soldiers.get(r)/(i);
			}
		};
		
		new RegionFactor.RegionFactorImp(init.tmpLoyalty, DicArmy.¤¤Army, DicArmy.¤¤Army) {
			
			@Override
			public double getD(Region t) {
				if (t.faction() != FACTIONS.player())
					return 1;
				double power = 0;
				for (WArmy a : FACTIONS.player().kingdom().armies().all())
					if (a.region() == t)
						power += WARMYD.quality().get(a);
				return 1.0 + power/(REGIOND.POP().total.get(t)+1);
			}
			
			@Override
			public double next(Region r) {
				return getD(r);
			}
		};

		
		soldiers_target = new RegionFactors(soldiers.info()) {
			
			@Override
			public double getD(Region t) {
				return soldiers.get(t);
			}
			
		};
		
		new RegionFactor.RegionFactorImp(soldiers_target, DicMisc.¤¤Population, DicMisc.¤¤Population) {
			
			@Override
			public double getD(Region t) {
				int i = (int) (RES.config().BATTLE.REGION_MAX_MEN*REGIOND.POP().popValue(t));
				i = CLAMP.i(i, 0, RES.config().BATTLE.REGION_MAX_MEN);
				return i;
			}
			
			@Override
			public double next(Region r) {
				return getD(r);
			}
		};
		
		decreeSoldiers.connect(soldiers_target, 0, 1.0/decreeSoldiers.max(null));
		
	}

	public void extractSpoils(Region r, int[] equipAmounts) {
		if (FACTIONS.player().capitolRegion() == r) {
			World.ARMIES().extractLostEquipment(equipAmounts);
		}
	}
	


	@Override
	void update(Region r, double ds) {
		

		
		if (r != FACTIONS.player().capitolRegion()) {
			int t = (int) soldiers_target.next(r);
			int n = soldiers.get(r);
			
			if (r.faction() != null && r.faction().capitolRegion() == r) {
				t = RES.config().BATTLE.REGION_MAX_MEN;
			}
		
			if (t > n) {
				double d = t-n;
				d /= 16;
				
				n += (int) d;
				d -= (int) d;
				if (d > RND.rFloat()) {
					n++;
				}
			}else {
				n -= 100;
			}
			
			n = CLAMP.i(n, 0, t);
		
			
			soldiers.set(r, n);
				
		}
		
		
	}

	@Override
	SAVABLE saver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void remove(Region r, FRegions old) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void add(Region r, FRegions newR) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void generateInit(Region r) {
		decreeSoldiers.set(r, 1 + RND.rInt(decreeSoldiers.max(null)));
		soldiers.set(r, (int) soldiers_target.next(r));
	}
	
	private final ArrayList<WDiv> divs = new ArrayList<WDiv>(128);
	private final ArrayList<WDIV> res = new ArrayList<>(128);
	
	{
		while(divs.hasRoom())
			divs.add(new WDiv());
	}
	
	public LIST<WDIV> divisions(Region r){
		if (FACTIONS.player().capitolRegion() == r) {
			return World.ARMIES().playerGarrison();
		}
		res.clear();
		double s = soldiers.get(r);
		if (s == 0)
			return res;
		
		double tot = REGIOND.POP().total.get(r);
		double inv = soldiers.get(r)/tot;
		int solRemaining = soldiers.get(r);
		
		int i = 0;
		int remain = 0;
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race ra = RACES.all().get(ri);
			int sols = (int) Math.ceil(inv*REGIOND.RACE(ra).population.get(r));
			if (sols > 16) {
				sols += remain;
			}
			if (sols > solRemaining)
				sols = solRemaining;
				
			while(sols > 8 && solRemaining > 0) {
				int m = CLAMP.i(sols, 0, RES.config().BATTLE.MEN_PER_DIVISION);
				solRemaining -= m;
				sols -= m;
				WDiv d = divs.get(i);
				d.index = i;
				d.f = r.faction();
				d.men = m;
				d.race = ra;
				d.r = r;
				res.add(d);
				i++;
			}
			remain += sols;
		}
		solRemaining = CLAMP.i(solRemaining, 0, RES.config().BATTLE.MEN_PER_DIVISION);
		if (solRemaining > 0) {
			Race big = FACTIONS.player().race();
			int bb = 0;
			for (Race ra : RACES.all()) {
				if (REGIOND.RACE(ra).population.get(r) > bb) {
					big = ra;
					bb = REGIOND.RACE(ra).population.get(r);
				}
			}
			WDiv d = divs.get(i);
			d.index = i;
			d.f = r.faction();
			d.r = r;
			d.men = solRemaining;
			d.race = big;
			res.add(d);
		}
		return res;
	}
	
	
	
	private static class WDiv implements WDIV {

		Race race;
		int men;
		int index;
		Faction f;
		Region r;
		
		@Override
		public int men() {
			return men;
		}

		@Override
		public Race race() {
			return race;
		}

		@Override
		public int menTarget() {
			return men;
		}

		@Override
		public double experience() {
			return 0;
		}

		@Override
		public void disband() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void resolve(WInduStored[] hs) {
			menSet(hs.length);
		}
		@Override
		public void resolve(int surviviors, double experiencePerMan) {
			menSet(surviviors);
		}
		
		@Override
		public void menSet(int amount) {
			REGIOND.MILITARY().soldiers.inc(r, -(men-amount));
		}

		@Override
		public int daysUntilMenArrives() {
			return 0;
		}

		@Override
		public int amountOfMenThatWillArrive() {
			return 0;
		}

		@Override
		public void hover(GBox box) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int type() {
			return 0;
		}

		@Override
		public void reassign(WArmy a) {
			
		}

		@Override
		public CharSequence name() {
			return DicArmy.¤¤Garrison;
		}

		@Override
		public WArmy army() {
			return null;
		}

		@Override
		public int equipTarget(EQUIPPABLE_MILITARY e) {
			if (e == STATS.EQUIP().BATTLEGEAR)
				return 2;
			return 0;
		}

		@Override
		public boolean needSupplies() {
			return false;
		}

		@Override
		public double training_melee() {
			if (f != null && f.capitol().r() == r)
				return 0.75;
			return 0.15;
		}

		@Override
		public double training_ranged() {
			return 0;
		}

		@Override
		public DivisionBanner banner() {
			return SETT.ARMIES().banners.get(index);
		}

		@Override
		public void bannerSet(int bi) {
			
		}

		@Override
		public Faction faction() {
			return f;
		}
		
		@Override
		public WDivGeneration generate() {
			return WINDU.generate(this, army());
		}

		@Override
		public double equip(EQUIPPABLE_MILITARY e) {
			return equipTarget(e);
		}

		@Override
		public int bannerI() {
			return index;
		}


		
		
	}
	
	
}
