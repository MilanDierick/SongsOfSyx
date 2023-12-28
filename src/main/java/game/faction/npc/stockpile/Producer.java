package game.faction.npc.stockpile;

import game.faction.FResources.RTYPE;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.Updater.SIns;
import game.faction.npc.stockpile.Updater.SOutput;
import init.resources.RESOURCES;
import settlement.room.main.FlatIndustries.FlatIndustry;
import util.data.DOUBLE_O;

final class Producer {

	void set(Updater os, FactionNPC faction, DOUBLE_O<FlatIndustry> bonuses, NPCStockpile stockpile, double workforce) {
		
		for (SIns ii : os.allIns) {
			ii.clear();
			ii.prodSpeed = 1.0/ii.out.rate;
			ii.prodSpeedBonus = 1.0/(ii.out.rate*bonuses.getD(ii.ins));
		}
		
		for (SIns ii : os.allIns) {
			ii.prodSpeedTot = getManhours(faction, ii, bonuses);
			
		}
		
		double totPP = 0;
		for (SOutput o : os.allO) {
			double p = Double.MAX_VALUE;
			for (SIns i : o.producers) {
				p = Math.min(p, i.prodSpeedTot);
			}
			o.prodSpeed = 1.0/p;
			stockpile.prodCap[o.res.index()] = o.prodSpeed;
			totPP += o.prodSpeed;
			
		}
		
		for (SOutput o : os.allO) {
			o.amount = totPP*((stockpile.amount(o.res)+1.0)/(stockpile.total()+1.0));
		}
		
		double totPrio = 0;
		for (SOutput o : os.allO) {
			o.prio = 1.0 - o.amount/o.prodSpeed;
			if (o.prio > 0)
				totPrio += o.prio;
			else
				o.prio = 0;
			
			stockpile.prio[o.res.index()] = (short) (100*o.prio);
			
		}
		
		if (totPrio <= 0) {
			
			for (SOutput o : os.allO) {
				o.prio = 1.0/RESOURCES.ALL().size();
			}
		}else {
			for (SOutput o : os.allO) {
				o.prio /= totPrio;
			}
		}
			
		
		
		
		for (SOutput o : os.allO) {
			double wf = o.prio*workforce;
			double am = o.prodSpeed*wf;
			stockpile.inc(o.res, am);
			faction.res().inc(o.res, RTYPE.PRODUCED, (int)am);
			o.produced = am;
		}
		
	}
	
	

	
	private double getManhours(FactionNPC faction, SIns o, DOUBLE_O<FlatIndustry> bonuses) {
		
		
		
		if (o.inputs.size() == 0)
			return o.prodSpeedBonus;
		
		double d = 0;
		
		
		
		for (int oi = 0; oi < o.inputs.size(); oi++) {
			
			
			
			double inRate = o.ins.industry.ins().get(oi).rate/o.ins.industry.ins().get(oi).resource.conBoost(faction.bonus);
			double outRate = o.out.rate;
			
			
			
			double am = inRate/outRate;
			
			
			
			d +=  am*getManhours(faction, o.inputs.get(oi), bonuses);
		}
		
		return d + o.prodSpeedBonus;
	}
	
	private double getManhours(FactionNPC faction, SOutput o, DOUBLE_O<FlatIndustry> bonuses) {
		
		double m = Double.MAX_VALUE;
		
		for (SIns in : o.producers) {
			m = Math.min(m, getManhours(faction, in, bonuses));
		}
		
		return m;
		
	}
	


	
}
