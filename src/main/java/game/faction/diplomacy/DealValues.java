package game.faction.diplomacy;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.trade.TradeManager;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import snake2d.util.misc.CLAMP;
import view.main.VIEW;
import world.regions.Region;
import world.regions.data.RD;

public final class DealValues {

	private static FactionNPC nCacheF;
	private static int ncacheI = -1;
	private static double nvalue;
	
	private static int cPI = -1;
	private static double cpD = 0;
	public static double netValue(Deal deal, Faction owner, FactionNPC buyer) {
		
		if (owner == FACTIONS.player()) {
			if (cPI == VIEW.RI())
				return cpD;
			cPI = VIEW.RI();
			double v = credits(owner);
			for (Region reg : DealRegions.getOfferable(deal, owner)) {
				v += DealRegions.valueRegion(reg, deal);
			}
			for (RESOURCE res : RESOURCES.ALL()) {
				v += valueResource(res, owner, buyer, (int) (SETT.ROOMS().STOCKPILE.tally().amountReservable(res)*0.75));
			}
			cpD = v*0.5;
			return cpD;
			
		}else if (owner instanceof FactionNPC) {
			if (nCacheF == owner && VIEW.RI() == ncacheI)
				return nvalue;
			nCacheF = (FactionNPC) owner;
			ncacheI = VIEW.RI();
			
			double v = credits(owner);
			for (Region reg : DealRegions.getOfferable(deal, owner)) {
				v += DealRegions.valueRegion(reg, deal);
			}
			for (RESOURCE res : RESOURCES.ALL()) {
				v += valueResource(res, owner, buyer, (int) (buyer.res().get(res)*0.75));
			}
			nvalue = v;
			return nvalue*0.5;
		}
		return 0;
		
		
	}
	
	public static double norValue(double rawValue, FactionNPC f) {
		return rawValue/(CLAMP.d(f.stockpile.credit(), 1, Double.MAX_VALUE));
	}
	
	public static double credits(Faction f) {
		if (f == FACTIONS.player()) {
			return Math.max(FACTIONS.player().credits().credits(), 1);
			
		}else if (f instanceof FactionNPC) {
			return Math.max(((FactionNPC)f).buyer().credits(), 1);
		}
		return 0;
	}
	
	
	public static double valueResource(RESOURCE res, Faction owner, FactionNPC buyer, int amount) {
		
		if (owner == FACTIONS.player()) {
			int p = buyer.seller().buyPrice(res, amount);
			p -= TradeManager.toll(FACTIONS.player(), buyer, RD.DIST().distance(buyer), p);
			p *= 0.75;
			return Math.max(p, 0);
		}else {
			int p = buyer.buyer().priceSell(res, amount);
			p += TradeManager.toll(FACTIONS.player(), buyer, RD.DIST().distance(buyer), p);
			p *= 1.25;
			return Math.max(p, 0);
		}
		
	}
	
//	public static double valueRegion(Region reg, FactionNPC faction) {
//		
//		return valueRegion(DealRegions.valueRegion(reg, faction), reg, faction);
//		
//	}
//	
//	public static double valueRegion(double v, Region reg, FactionNPC faction) {
//		
//		v*= RD.RACES().popSize(reg);
//		double res = 0;
//		for (RDBuilding bu : RD.BUILDINGS().all) {
//			for (BoostSpec bo : bu.boosters().all()) {
//				RESOURCE resource = RD.OUTPUT().fromBoost(bo.boostable);
//				if (resource != null) {
//					double m = 1;
//					for (BoostSpec b : bu.baseFactors)
//						m *= b.get(reg);
//					res += bo.booster.max()*m*faction.stockpile.priceBuy(resource.index(), 1);
//					
//				}
//			}
//		}
//		return res*v;
//		
//	}
	
}
