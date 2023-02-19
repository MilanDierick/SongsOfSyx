package game.faction.npc;

import game.faction.FACTIONS;
import game.faction.trade.TradeManager;
import init.RES;
import init.race.RACES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import world.World;
import world.map.regions.REGIOND;
import world.map.regions.Region;
import world.map.regions.RegionTaxes.RegionResource;

public final class UpdaterNPC {
	
	void update(FactionNPC f) {
		populateNConsume(f);
		addMin(f);
		produce(f);
	}
	
	private void populateNConsume(FactionNPC f) {
		
		double pop = 1;
		for (RESOURCE r : RESOURCES.ALL()) {
			int d = f.buyer().consume(r);
			pop += Math.pow(d, 0.45);
		}
		double min = Math.min(pop, f.capitol().population.total().get());
		double max = Math.max(pop, f.capitol().population.total().get());
		double delta = max-min;
		double inc = Math.pow(delta, 0.5);
		int p = (int) (min +inc);
		
		f.capitol().population.set(RACES.all().get(0), (int) p);
	}
	

	
	private void produce(FactionNPC f) {
		
		NPCProduction.produce(f);
//		
//		double pop = (f.capitol().population.total().get()/100000.0);
//		pop = CLAMP.d(pop, 0, pop);
//		
//		for (Industry i : resource.ins()) {
//			
//			//Cost of raw materials + cost of most expensive raw material that can be produced in the same time. And a little overhead.
//			for (IndustryResource out : i.outs()) {
//				if (i.ins().size() == 0) {
//					continue;
//				}
//				
//				double hPrice = 0;
//				double price = 0;
//				for (IndustryResource in : i.ins()) {
//					RESOURCE r = in.resource;
//					double a = f.buyer().amount(in.resource);
//					for (RESOURCE rr : r.tradeSameAs()) {
//						if (f.buyer().amount(rr) > a) {
//							r = rr;
//							a = f.buyer().amount(rr);
//						}
//					}
//					
//					double needed = in.rate / (out.rate*resource.mul(i.bonus()));
//					
//					double p = needed/(f.buyer().amount(r)+1);
//					price += p;
//					hPrice = Math.max(p, hPrice);
//				}
//				
//				price += hPrice;
//				//price /= (out.rate+FACTIONS.player().bonus().maxAdd(i.bonus())/2);
//			
//				
//				double am = 1.0/(price*(1.0 + i.ins().size()*0.1));
//				
//				am *= CLAMP.d(resource.rate(out.resource)*pop, 0, 1);
//				
//				am = CLAMP.d(am-f.buyer().amount(out.resource), 0, am);
//				
//				f.buyer().add(out.resource, (int)am/2);
//				
//			}
//		}
		
	}
	
	private void addMin(FactionNPC f) {
		
		for (RESOURCE r : RESOURCES.ALL()) {
			f.buyer().add(r, nAmount(f, r));
		}
		
	}
	
	public void init(TradeManager trade) {
		
		RES.loader().print("Simulating factions...");
		
		
		
		for (FactionNPC f : FACTIONS.NPCs()) {
			if (!f.isActive())
				continue;
			init(f);
		}
		
		int a = 50;
		
		for (int i = 0; i < a*2; i++) {
			RES.loader().print("Simulating factions " + (int)(100*(i/(double)(a*4))) + "%");
			for (FactionNPC f : FACTIONS.NPCs()) {
				
				if (!f.isActive())
					continue;
				addMin(f);
				gather(f);
				populateNConsume(f);
				produce(f);
			}
		}

		
		for (int i = 0; i < a; i++) {
			RES.loader().print("Simulating factions " + (int)(100*((i*2+a*2)/(double)(a*4))) + "%");
			for (FactionNPC f : FACTIONS.NPCs()) {
				
				if (!f.isActive())
					continue;
				addMin(f);
				gather(f);
				populateNConsume(f);
				produce(f);
			}
			if (i % 4 == 0) {
				trade.prime();
			}
		}
		trade.prime();
	}
	
	private void init(FactionNPC f) {
//
//		for (Region reg : f.kingdom().realm().regions()) {
//			for (RegionResource rd : REGIOND.RES().res) {
//				double rate = rd.outputAI(reg);
//				
//				double am = TradeNPC.CONSUMPTION_DIVIDER*rate;
//				f.buyer().add(rd.resource, (int) (am));
//				
//			}
//		}
//
//		double pop = 1;
//		for (RESOURCE r : RESOURCES.ALL()) {
//			f.buyer().add(r, (int) (nAmount(f, r)*TradeNPC.CONSUMPTION_DIVIDER));
//			int d = f.buyer().consume(r);
//			pop += Math.pow(d, 0.45);
//		}
//		
//		f.capitol().population.set(RACES.all().get(0), (int)pop);
//		produce(f);
//		produce(f);
//		produce(f);
//		produce(f);
	}
	
	private int nAmount(FactionNPC f, RESOURCE res) {
		return 128; //(int) (resource.rateTot(res)*f.kingdom().realm().regions().size()*128);
	}

	private void gather(FactionNPC f) {
		if (!f.isActive())
			return;
		for (Region reg : f.kingdom().realm().regions()) {
			int[] ams = World.REGIONS().outputter.getAmounts(f, reg);
			int i = 0;
			for (RegionResource rd : REGIOND.RES().res) {
				int am = ams[i++];
				f.buyer().add(rd.resource, am);
				f.res().inTaxes.inc(rd.resource, am);
			}
		}
	}
	
}
