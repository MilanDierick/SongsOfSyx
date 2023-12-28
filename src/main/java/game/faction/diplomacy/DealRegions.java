package game.faction.diplomacy;


import game.GAME;
import game.boosting.BoostSpec;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import init.resources.RESOURCE;
import snake2d.util.misc.CLAMP;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.*;
import view.main.VIEW;
import world.WORLD;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;
import world.regions.Region;
import world.regions.WREGIONS;
import world.regions.data.RD;
import world.regions.data.RD.RDOwnerChanger;
import world.regions.data.building.RDBuilding;

public final class DealRegions {

	private static int[] values = new int[WREGIONS.MAX];
	private static final CachedList A = new CachedList();
	private static final CachedList B = new CachedList();
	private static int changeI = -1;
	private static int upI = -120;
	
	private static Faction giver;
	private static Faction reciever;
	private final static WTREATY t = new WTREATY() {
		
		@Override
		public boolean can(int fx, int fy, int tx, int ty, double dist) {
			Region from = WORLD.REGIONS().map.get(fx, fy);
			Region to = WORLD.REGIONS().map.get(tx, ty);
			if (from == null || to == null)
				return true;
			if (from == to)
				return true;
			if (from.faction() == giver || to.faction() == giver)
				return true;
			if (from.faction() == reciever || to.faction() == reciever)
				return true;
			return false;
		}
	};
	
	private final static WRegSel sel = new WRegSel() {
		
		@Override
		public boolean is(Region t) {
			return t.faction() == giver && !t.capitol();
		}
	};
	
	static LIST<Region> getOfferable(Deal deal, Faction giver){
		
		init(deal);
		if (giver == A.faction)
			return A.all;
		return B.all;
	}
	
	public static double valueRegion(Region reg, Deal deal) {
		
		init(deal);
		return values[reg.index()];
	}
		
	public static boolean canOffer(Deal deal, Faction f, Region reg, LIST<Region> offered) {
		
		init(deal);
		return Offerer.canOffer(f, reg, offered);
		
	}
	
	private static class Offerer {
		
		private static int cache = -1;
		private static int cacheRegs = 0;
		private static IntChecker rCheck = new IntChecker(WREGIONS.MAX);
		private static Faction f;
		
		private static final WTREATY treaty = new WTREATY() {
			
			@Override
			public boolean can(int fx, int fy, int tx, int ty, double dist) {
				Region to = WORLD.REGIONS().map.get(tx, ty);
				if (to == null)
					return true;
				if (to.faction() == f)
					return true;
				
				if (to.faction() == FACTIONS.player()) {
					Region from = WORLD.REGIONS().map.get(fx, fy);
					if (to == from)
						return true;
					if (from == null)
						return true;
					if (from.faction() == f)
						return true;
					if (from.faction() == FACTIONS.player())
						return rCheck.isSet(from.index());
					return false;
				}
				
				return false;
			}
		};
		
		public static boolean canOffer(Faction f, Region reg, LIST<Region> offered) {
			
			if (reg.capitol())
				return false;
			
			if (reg.faction() != FACTIONS.player()) {
				return values[reg.index()] > 0;
			}
			
			
			if (Offerer.f == f && Offerer.cache == VIEW.RI() && Offerer.cacheRegs == offered.size())
				return Offerer.rCheck.isSet(reg.index());
			
			Offerer.f = f;
			Offerer.cache = VIEW.RI();
			Offerer.cacheRegs = offered.size();
			
			Offerer.rCheck.init();
			for (Region r : offered)
				Offerer.rCheck.isSetAndSet(r.index());
			
			LIST<RDist> dd = WORLD.PATH().tmpRegs.all(f.capitolRegion(), Offerer.treaty, WRegSel.FACTION(FACTIONS.player()));
			Offerer.rCheck.init();
			for (RDist d : dd)
				Offerer.rCheck.isSetAndSet(d.reg.index());
			return Offerer.rCheck.isSet(reg.index());
			
		}
		
	}

	private static class CachedList {
		
		private Bitmap1D canOffer = new Bitmap1D(WREGIONS.MAX, false);
		private Faction faction;
		private ArrayList<Region> all = new ArrayList<>(128);
		
	}
	
	private static boolean init(Deal deal) {
		

		if (deal.player.f.get() != A.faction)
			return initt(deal);
		
		if (deal.player.f.get() != B.faction)
			return initt(deal);
		
		if (RDOwnerChanger.changeI != changeI)
			return initt(deal);
		
		if (Math.abs(upI-GAME.updateI()) > 0)
			return initt(deal);
		
		return true;
	}
	
	private static boolean initt(Deal deal) {
		
		A.faction = deal.player.f.get();
		B.faction = deal.faction();
		changeI = RDOwnerChanger.changeI;
		upI = GAME.updateI();
		
		init(A, B, deal.faction());
		init(B, A, deal.faction());
		
		return true;
	}
	
	private static void init(CachedList A, CachedList B, FactionNPC npc) {
		A.all.clearSloppy();
		DealRegions.giver = A.faction;
		DealRegions.reciever = B.faction;
		A.canOffer.clear();
		
		for (RDist d : WORLD.PATH().tmpRegs.all(reciever.capitolRegion(), t, sel)){
			if (A.all.hasRoom())
				A.all.add(d.reg);
			A.canOffer.set(d.reg.index(), true);
			
			values[d.reg.index()] = (int) Math.ceil(valueRegion(d.reg, d.distance, npc));
		}
	}
	
	private static double valueRegion(Region reg, double distance, FactionNPC faction) {
		
		double value = 1;
		if (reg.faction() == faction || RD.OWNER().prevOwner(reg) == faction)
			value *= 2;
		else
			value *= 0.5 + 0.5*CLAMP.d(1-distance/255.0, 0, 1);	
		
		double res = RD.RACES().popSize(reg)*NPCStockpile.AVERAGE_PRICE*(1+faction.stockpile.creditScore());
		for (RDBuilding bu : RD.BUILDINGS().all) {
			for (BoostSpec bo : bu.boosters().all()) {
				RESOURCE resource = RD.OUTPUT().fromBoost(bo.boostable);
				if (resource != null) {
					double m = 1;
					for (BoostSpec b : bu.baseFactors)
						m *= b.get(reg);
					res += bo.booster.max()*m*faction.stockpile.price(resource.index(), 1);
					
				}
			}
		}
		return res*value;
		
	}
	
}

