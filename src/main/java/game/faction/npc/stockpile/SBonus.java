package game.faction.npc.stockpile;

import game.faction.npc.FactionNPC;
import settlement.main.SETT;
import settlement.room.main.FlatIndustries.FlatIndustry;
import util.data.DOUBLE_O;
import world.regions.Region;
import world.regions.data.RD;

final class SBonus implements DOUBLE_O<FlatIndustry> {

	private double[] bo = new double[SETT.ROOMS().FIndustries.all().size()];
	
	@Override
	public double getD(FlatIndustry t) {
		
		return bo[t.index()];
	}
	
	void init(FactionNPC f) {
		
		for (FlatIndustry ins : SETT.ROOMS().FIndustries.all()) {
			
			
			double b = 0;
			double size = 0;
			for (int i = 0; i < f.realm().regions(); i++) {
				Region reg = f.realm().region(i);
				double pop = 100 + RD.RACES().population.getD(reg);
				b += ins.industry.getRegionBonus(reg)*pop;
				size += pop;
			}
			b /=  size;
			
			bo[ins.index] = b*ins.industry.bonus().get(f.bonus);
			
		}
	}
	

	
}