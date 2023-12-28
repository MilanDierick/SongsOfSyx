package game.faction.diplomacy;

import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.LOG;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.regions.Region;

public class DealDrawfter {

	public static void draftPeace(Deal deal, FactionNPC enemy) {
		
		deal.setFactionAndClear(enemy);
		
		deal.peace.set(true);
		
		double v = deal.valueCredits();
		
		if (v < 0) {
			draftPeace(deal, -v, deal.player, deal.npc, enemy);
		}else {
			draftPeace(deal, v, deal.npc, deal.player, enemy);
		}
		
	}
	
	public static void draft(Deal deal) {
		
		double v = deal.valueCredits();
		FactionNPC enemy = deal.faction();

		if (v == 0)
			return;
		
		if (v < 0) {
			draftPeace(deal, -v, deal.player, deal.npc, enemy);
		}else {
			draftPeace(deal, v, deal.npc, deal.player, enemy);
		}
		deal.dupI = -1;
	}
	
	public static void draft(Deal deal, double dcreds) {
		
		if (debug)
			LOG.ln(dcreds + " " + deal.valueCredits());
		double v = deal.valueCredits()+dcreds;
		FactionNPC enemy = deal.faction();

		if (v == 0)
			return;
		
		if (v < 0) {
			draftPeace(deal, -v, deal.player, deal.npc, enemy);
		}else {
			draftPeace(deal, v, deal.npc, deal.player, enemy);
		}
		deal.dupI = -1;
	}
	
	private static boolean debug = false;
	
	
	private static void draftPeace(Deal deal, double v, DealParty giver, DealParty getter, FactionNPC enemy) {
		
		if (debug)
			LOG.ln(v);
		
		int creds = (int) CLAMP.d(DealValues.credits(giver.f.get())*0.9, 0, v);
		
		if (debug)
			LOG.ln(creds);
		
		
		if (creds >= v*2) {
			giver.credits.set((int) Math.ceil(v));
			return;
		}
		
		double res = 0;
		for (RESOURCE r : RESOURCES.ALL()) {
			if (debug) {
				LOG.ln(r.key + " " + giver.f.get().res().get(r) + " " + DealValues.valueResource(r, giver.f.get(), enemy, (int)giver.f.get().res().get(r)));
			}
			
			res += DealValues.valueResource(r, giver.f.get(), enemy, (int)giver.f.get().res().get(r))*0.5;
		}
		
		if (debug) {
			LOG.ln(res);
			LOG.ln(giver.regs.size());
		}
		double regs = 0;
		
		for (Region reg : DealRegions.getOfferable(deal, giver.f.get())) {
			
			
			double vd = DealRegions.valueRegion(reg, deal);
			if (debug)
				LOG.ln(vd);
			if (creds + res + regs + vd < v) {
				giver.regs.add(reg);
				regs += vd;
			}else if (creds + regs + vd <= v) {
				giver.regs.add(reg);
				regs += vd;
			}else
				break;
		}
		
		if (debug) {
			LOG.ln(regs);
			LOG.ln(giver.regs.size());
		}
		
		if (regs + creds >= v*2) {
			giver.credits.set((int) (v-regs));
			return;
		}
		
		int[] rr = new int[RESOURCES.ALL().size()];
		for (int i = 0; i < rr.length; i++)
			rr[i] = i;
		for (int i = 0; i < rr.length; i++) {
			int o = rr[i];
			int ii = RND.rInt(rr.length);
			rr[i] = rr[ii];
			rr[ii] = o;
		}
		
		res = 0;
		for (int ri : rr) {
			RESOURCE r = RESOURCES.ALL().get(ri);
			int am = (int) (giver.f.get().res().get(r)*0.5);
			double va = DealValues.valueResource(r, giver.f.get(), enemy, am);
			
			if (creds*0.5 + regs + res + va > v) {
				
				double ta = v - creds*0.5 - res-regs;
				am = (int) Math.ceil(am*ta/va);
				giver.resources.inc(r, am);
				res += DealValues.valueResource(r, giver.f.get(), enemy, am);

				break;
			}else {
				giver.resources.inc(r, am);
				res += va;
			}
		}
		
		int cc = (int) (v-regs-res);
		cc = CLAMP.i(cc, 0, creds-giver.credits.get());
		
		giver.credits.inc(cc);
		
	}
	
	//private static final WTREATY 
	
}
