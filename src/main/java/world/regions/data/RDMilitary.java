package world.regions.data;

import game.GAME;
import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.D;
import init.config.Config;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.entity.ENTETIES;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O;
import util.data.INT_O.INT_OE;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.info.INFO;
import world.WORLD;
import world.army.WDIV;
import world.army.WDivGeneration;
import world.regions.Region;
import world.regions.data.RD.RDInit;
import world.regions.data.RD.RDUpdatable;
import world.regions.data.RData.RDataE;
import world.regions.data.pop.RDRace;

public class RDMilitary {
	
	private static CharSequence ¤¤garrisonD = "Troops that are stationed in a region that will defend it against attacks.";
	private static CharSequence ¤¤conscriptD = "Conscripts are candidates that can be trained into soldiers.";
	
	static {
		D.ts(RDMilitary.class);
	}
	
	public final RDataE garrison;
	public final Boostable bgarrison;
	public final Boostable conscriptTarget;
	public final Boostable defences;
	
	RDMilitary(RDInit init){
		
		bgarrison = BOOSTING.push("GARRISON", 0, DicMisc.¤¤garrison, ¤¤garrisonD, UI.icons().s.shield, BoostableCat.WORLD);
		defences = BOOSTING.push("FORTIFICATION", 1, DicArmy.¤¤Fort, DicArmy.¤¤FortD, UI.icons().s.degrade, BoostableCat.WORLD);
		
		INT_OE<Region> dd = init.count.new DataShort(null, Config.BATTLE.REGION_MAX_DIVS*Config.BATTLE.MEN_PER_DIVISION) {
			@Override
			public int get(Region t) {
				if (FACTIONS.player().capitolRegion() == t) {
					int pow = 0;
					for (WDIV d : WORLD.ARMIES().playerGarrison()) {
						pow += d.men();
					}
					return pow;
				}
				return super.get(t);
			}
			
			@Override
			public void set(Region t, int s) {
				lReg = null;
				super.set(t, s);
			}
		};
		garrison = new RDataE(dd, init, DicMisc.¤¤garrison);
		init.upers.add(new RDUpdatable() {
			private final double dt = 2.0/(TIME.secondsPerDay);
			
			@Override
			public void update(Region reg, double time) {
				double d = garrisonTarget(reg)-garrison.get(reg);
				garrison.incFraction(reg, dt*time*50*Math.signum(d));
				
			}

			@Override
			public void init(Region reg) {
				garrison.set(reg, (int)garrisonTarget(reg));
				
			}
		});
		
		conscriptTarget = BOOSTING.push("CONSCRIPTABLE_TARGET", 0, DicMisc.¤¤Conscripts, ¤¤conscriptD, UI.icons().s.sword, BoostableCat.WORLD);
		
		new RBooster(new BSourceInfo(DicMisc.¤¤Population, UI.icons().s.human), 0, 20000, false) {

			@Override
			public double get(Region t) {
				return RD.RACES().population.get(t)/400000.0;
			}
		}.add(conscriptTarget);
		
	}
	
	public int garrisonTarget(Region reg) {
		if (reg.faction() == FACTIONS.player()) {
			return (int) bgarrison.get(reg);
		}else {
			double dz = RD.RACES().population.get(reg)/(double)ENTETIES.MAX;
			dz *= 1 + 0.25*(-8 + RD.RAN().get(reg, 9, 4))/8.0;
			dz = CLAMP.d(dz, 0, 1);
			
			if (reg.faction() instanceof FactionNPC) {
				FactionNPC f = (FactionNPC) reg.faction();
				dz *= 0.5 + 0.5*f.court().king().garrison();
			}
			dz = CLAMP.d(dz, 0, 1);
			return (int) (100 + dz*(garrison.max(reg)-100));
		}
	}
	

	
	public int conscripts(Race r, Faction f) {
		if (f == FACTIONS.player()) {
			if (RD.RACES().get(r) == null) {
				return 0;
			}

			
			int am = 0;
			for (int i = 0; i < f.realm().regions(); i++) {
				Region rr = f.realm().region(i);
				if (!rr.capitol() && RD.RACES().population.get(rr) > 0) {
					am += conscriptTarget.get(rr)*RD.RACES().get(r).pop.get(rr)/RD.RACES().population.get(rr);
				}
			}
			
			
			return am;
			
		}else {
			if (RD.RACES().get(r) == null) {
				return WORLD.camps().factions.max(f, r);
			}
			
			double p = 1 + f.realm().all().size()*0.025;
			int t = (int) (RD.RACES().get(r).pop.faction().get(f)*0.15/p);
			return t;
		}
	}
	
	public final DOUBLE_O<Region> power = new DOUBLE_O<Region>() {
		private final INFO info = new INFO(DicArmy.¤¤Garrison, 
				DicArmy.¤¤GarrisonD);

			@Override
			public double getD(Region t) {
				int p = 0;
				for (WDIV d : divisions(t))
					p += d.provess();
				return p;
			}

			@Override
			public INFO info() {
				return info;
			}
	};
	
	public double garrison(Region reg) {
		return garrison.getD(reg);
	}
	
	private final ArrayList<WDiv> divs = new ArrayList<WDiv>(128);
	private final ArrayList<WDIV> res = new ArrayList<>(128);
	
	{
		while(divs.hasRoom())
			divs.add(new WDiv());
	}
	
	public void extractSpoils(Region r, int[] equipAmounts) {
		if (FACTIONS.player().capitolRegion() == r) {
			WORLD.ARMIES().extractLostEquipment(equipAmounts);
		}
	}
	
	private Region lReg = null;
	private int upI = -1;
	
	public LIST<WDIV> divisions(Region r){
		
		if (FACTIONS.player().capitolRegion() == r) {
			return WORLD.ARMIES().playerGarrison();
		}
		
		if (lReg == r && GAME.updateI() == upI)
			return res;
		
		
		lReg = r;
		upI = GAME.updateI();
		
		res.clear();
		int soldiers = (int)Math.max(garrison.get(r), garrisonTarget(r));
		
		
		
		if (soldiers == 0)
			return res;
		
		double dv = (double)garrison.get(r)/soldiers;
		dv = CLAMP.d(dv, 0, 1);
		
		int realSoldiers = garrison.get(r);
		int solRemaining = soldiers;
		
		double tot = RD.RACES().population.get(r)+1;
		int i = 0;
		int remain = 0;
		for (int ri = 0; ri < RD.RACES().all.size(); ri++) {
			RDRace ra = RD.RACES().all.get(ri);
			int sols = (int) Math.ceil(soldiers*ra.pop.get(r)/tot);
			if (sols > 50) {
				sols += remain;
			}
			if (sols > solRemaining)
				sols = solRemaining;
				
			while(sols > 50 && solRemaining > 0) {
				
				int m = CLAMP.i(sols, 0, Config.BATTLE.MEN_PER_DIVISION);
				solRemaining -= m;
				sols -= m;
				WDiv d = divs.get(i);
				d.index = i++;
				d.f = r.faction();
				int rs = (int) Math.ceil(m*dv);
				rs = CLAMP.i(rs, 0, realSoldiers);
				realSoldiers -= rs;
				d.men = rs;
				d.menTarget = m;
				d.race = ra.race;
				d.r = r;
				res.add(d);
			}
			remain += sols;
		}
		
		if (solRemaining > 0) {
			Race big = FACTIONS.player().race();
			int bb = 0;
			for (int ri = 0; ri < RD.RACES().all.size(); ri++) {
				RDRace ra = RD.RACES().all.get(ri);
				if (ra.pop.get(r) > bb) {
					big = ra.race;
					bb = ra.pop.get(r);
				}
			}
			while (solRemaining > 0) {
				int m = CLAMP.i(solRemaining, 0, Config.BATTLE.MEN_PER_DIVISION);
				
				WDiv d = divs.get(i);
				d.index = i++;
				d.f = r.faction();
				d.r = r;
				int rs = (int) Math.ceil(m*dv);
				rs = CLAMP.i(rs, 0, realSoldiers);
				realSoldiers -= rs;
				d.men = rs;
				d.menTarget = m;
				solRemaining -= m;
				d.race = big;
				res.add(d);
			}
		}
		
		return res;
	}
	
	private static class WDiv implements WDIV {

		Race race;
		int men;
		int menTarget;
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
			return menTarget;
		}

		@Override
		public double experience() {
			return 0.1;
		}

		@Override
		public void resolve(Induvidual[] hs) {
			menSet(hs.length);
		}
		@Override
		public void resolve(int surviviors, double experiencePerMan) {
			menSet(surviviors);
		}
		
		void menSet(int amount) {
			RD.MILITARY().garrison.inc(r, -(men-amount));
		}

		@Override
		public int daysUntilMenArrives() {
			return 0;
		}

		@Override
		public CharSequence name() {
			return DicArmy.¤¤Garrison;
		}

		@Override
		public int equipTarget(EquipBattle e) {
			return e.garrisonAmount();
		}

		@Override
		public boolean needSupplies() {
			return false;
		}

		@Override
		public double training(StatTraining tr) {
			if (f != null && f.capitolRegion() == r)
				return 0.75;
			return 0.15;
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
			return new WDivGeneration(this);
		}

		@Override
		public double equip(EquipBattle e) {
			return equipTarget(e);
		}

		@Override
		public int bannerI() {
			return index;
		}

		@Override
		public double trainingTarget(StatTraining tr) {
			return training(tr);
		}


		
		
	}

	
}