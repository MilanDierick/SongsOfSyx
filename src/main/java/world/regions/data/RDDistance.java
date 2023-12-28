package world.regions.data;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.D;
import init.sprite.UI.UI;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.DicMisc;
import world.WORLD;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;
import world.regions.Region;
import world.regions.data.RD.RDInit;
import world.regions.data.RDOutput.RDResource;
import world.regions.data.pop.RDRace;

public class RDDistance {

	private static CharSequence ¤¤Name = "¤Proximity";
	private static CharSequence ¤¤NameD = "¤Proximity is the physical distance from a region to your capital. It determines the amount tribute you receive from it and the loyalty of its subjects.";

	private static CharSequence ¤¤Distance = "¤Distance";
	private static CharSequence ¤¤DistanceD = "¤Distance to your capital. Distance affect trade prices.";

	
	private final INT_OE<Region> data;
	private final INT_OE<Faction> factionReachable;
	private final INT_OE<Region> regionReachable;
	public final Boostable boostable;

	static {
		D.ts(RDDistance.class);
	}
	
	RDDistance(RDInit init) {
		data = init.count.new DataShort(¤¤Distance, ¤¤DistanceD);
		factionReachable = init.rCount.new DataBit();
		regionReachable = init.count.new DataBit();
		
		boostable = BOOSTING.push("PROXIMITY", 1, ¤¤Name, ¤¤NameD, UI.icons().s.wheel, BoostableCat.WORLD);
		
		new RBooster(new BSourceInfo(DicMisc.¤¤Distance, UI.icons().s.wheel), 1, 0.01, true) {

			final double II = 1.0/1024;
			
			@Override
			public double get(Region t) {
				return CLAMP.d(data.get(t)*II, 0, 1);
			}
			
		}.add(boostable);
		
		init.connectable.add(new ACTION() {
			
			@Override
			public void exe() {
				RBooster bo = new RBooster(new BSourceInfo(boostable.name, UI.icons().s.wheel), 0.01, 1, true) {
					@Override
					public double get(Region t) {
						if (t.faction() != FACTIONS.player())
							return 1;
						return CLAMP.d(boostable.get(t), 0, 1);
					}
					
					
				};
				for (RDRace r : RD.RACES().all) {
					bo.add(r.loyalty.target);
				}
				for (RDResource o : RD.OUTPUT().all)
					bo.add(o.boost);
			}
		});
	}

	void init() {
		Region cap = FACTIONS.player().capitolRegion();
		if (cap == null)
			return;
		for (Region reg : WORLD.REGIONS().all()) {
			regionReachable.set(reg, 0);
			data.setD(reg, 0);
		}
		for (Faction f : FACTIONS.active())
			factionReachable.set(f, 0);
		
		for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
			Region reg = FACTIONS.player().realm().region(ri);
			regionReachable.set(reg, 1);
			for (RDist d : WORLD.PATH().tmpRegs.all(reg, WTREATY.NEIGHBOURS(reg), WRegSel.DUMMY())) {
				regionReachable.set(d.reg, 1);
			}
		}
		
		for (RDist d : WORLD.PATH().tmpRegs.all(cap, treaty, WRegSel.DUMMY())) {
			
			if (d.reg.faction() != null && d.reg.faction() != FACTIONS.player()) {
				factionReachable.set(d.reg.faction(), 1);
			}
		}
		
		for (RDist d : WORLD.PATH().tmpRegs.all(cap, WTREATY.DUMMY(), WRegSel.DUMMY())) {
			data.set(d.reg, CLAMP.i(d.distance, 0, data.max(null)));
		}
	}
	
	public int distance(Faction f) {
		return data.get(f.capitolRegion());
	}
	
	public final INT_O<Region> distance(){
		return data;
	}
	
	public boolean factionIsAlmostReachable(Faction f) {
		return factionReachable.get(f) == 1;
	}
	
	public boolean factionBordersPlayer(Faction f) {
		for (int ri = 0; ri < f.realm().regions(); ri++) {
			if (regionReachable.get(f.realm().region(ri)) == 1)
				return true;
		}
		return false;
	}
	
	public boolean regionBordersPlayer(Region reg) {
		return regionReachable.get(reg) == 1;
	}
	
	private final WTREATY treaty = new WTREATY() {
		
		@Override
		public boolean can(int fx, int fy, int tx, int ty, double dist) {
			Region from = WORLD.REGIONS().map.get(fx, fy);
			if (from == null)
				return true;
			
			if (from.faction() == FACTIONS.player())
				return true;
			Region to = WORLD.REGIONS().map.get(tx, ty);
			if (to == null)
				return true;
			
			if (from.faction() == to.faction())
				return true;
			return false;
		}
	};

	
}
