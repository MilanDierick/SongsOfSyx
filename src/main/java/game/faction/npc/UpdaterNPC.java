package game.faction.npc;

import game.faction.FACTIONS;
import game.faction.trade.TradeManager;
import game.time.TIME;
import init.RES;
import world.regions.data.RD;

public final class UpdaterNPC {
	
	public void in2it(TradeManager trade) {
		
		RES.loader().print("Simulating factions...");
		
		int a = 50;
				
		for (int i = 0; i < a; i++) {
			RES.loader().print("Simulating factions " + (int)(100*((i*2+a*2)/(double)(a*4))) + "%");
			
			for (FactionNPC f : FACTIONS.NPCs()) {
				RD.UPDATER().shipAll(f, 1.0);
				f.stockpile.update(f, TIME.secondsPerDay);
			}
			
			if (i % 4 == 0) {
				trade.prime();
			}
		}
		trade.prime();
	}
	

	
}
