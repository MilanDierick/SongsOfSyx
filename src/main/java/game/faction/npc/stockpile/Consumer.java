package game.faction.npc.stockpile;

import java.util.Arrays;

import game.faction.FResources.RTYPE;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import init.resources.RESOURCES;

final class Consumer {

	public Consumer() {
		
	}
	
	public double[] consumed = new double[RESOURCES.ALL().size()];
	
	double consume(Updater os, FactionNPC f, double ds, NPCStockpile stockpile) {
		
		double need = 1;
		double cons = 1;
		Arrays.fill(consumed, 0);
		for (int ri = 0; ri < RESOURCES.ALL().size(); ri++) {
			RESOURCE res = RESOURCES.ALL().get(ri);
//			double c = os.needer.neededTot[ri];
//			if (c > 0) {
//				double am = Math.ceil(population*c);
//				need += am;
//				double available = stockpile.amount(ri);
//				if (available < am * 10) {
//					available *= available/(am*10);
//				}
//				available = CLAMP.d(available, 0, am);
//				cons += available;
//				stockpile.inc(res, -available);
//				consumed[ri] += available;
//				
//				
//			}
			
			double am = stockpile.amount(ri)*0.005*ds;
			stockpile.inc(res, -am);
			consumed[ri] += am;
			f.res().inc(res, RTYPE.CONSUMED, (int)-am);
		}
		
		return (cons)/(need);
		
	}
	
}
