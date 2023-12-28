package world.regions.data.gen;



import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.RES;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.WorldGen;
import world.map.pathing.*;
import world.map.pathing.WRegs.RDist;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.RD.RDInit;
import world.regions.data.pop.RDRace;

final class GeneratorFactions {

	private final WRegs rr = new WRegs();
	private static final int aveSize = 8;

	public GeneratorFactions() {

	}
	
	public void generate(RDInit init, WorldGen gen, ACTION loadprint) {
		
		loadprint.exe();
		
		
		
		generatePlayer(gen.playerX, gen.playerY);
		generateKingdoms();
		
		for (FactionNPC ff : FACTIONS.NPCs()) {
			RDRace race = null;
			double br = 0;
			
			Region r = ff.capitolRegion();
			
			for (RDRace rrr : RD.RACES().all) {
				if (rrr.pop.get(r) >= br) {
					br = rrr.pop.get(r);
					race = rrr;
				}
			}
			
			
			
			ff.generate(race, true);
			
			for (int ri = 0; ri < ff.realm().regions(); ri++) {
				r = ff.realm().region(ri);
				for (ACTION_O<Region> g : init.gens) {
					g.exe(r);
				}
				RD.UPDATER().BUILD(r);
				RD.RACES().initPopulation(r);
			}
			
			ff.generate(race, true);
		}
	}


	
	private void generatePlayer(int playerX, int playerY) {
		Region r = WORLD.REGIONS().map.get(playerX, playerY);
		r.fationSet(FACTIONS.player());
		r.setCapitol();
		r.info.name().clear().add(FACTIONS.player().name);
		
		LIST<RDist> ddd  = rr. all(r, WTREATY.DUMMY(), WRegSel.DUMMY(r));
		
		for (int i = 0; i < 3 && i < ddd.size(); i++) {
			Region n = ddd.get(i).reg;
			if (n == null)
				continue;
			create(n);
		}
		
		
	}
	
	private boolean create(Region reg) {
		if (FACTIONS.activateAvailable() == 0)
			return false;
		if (reg.realm() != null)
			return false;
		FACTIONS.activateNext(reg);
		return true;
	}
	
	
	private void generateKingdoms() {
		RES.loader().init();
		ArrayList<Region> regs = new ArrayList<>(WORLD.REGIONS().active());
		
		for (int i = 0; i < regs.size(); i++)
			regs.swap(RND.rInt(regs.size()), RND.rInt(regs.size()));
		
		int amount = 3*regs.size()/4;
		
		while(FACTIONS.activateAvailable() > 0 && amount > 0 && regs.size() > 0) {
			Region r = regs.removeLast();
			if (create(r)) {
				amount -= spread(r);
				
			}
		}
		
		
		
	}
	

	
	private int spread(Region home) {
		
		int amount = (int) (RND.rInt(aveSize*2));
		treaty.f = home.faction();
		LIST<RDist> ddd  = rr.all(home, treaty, WRegSel.DUMMY(home));
		int k = 1;
		for (int i = 0; i < amount && i < ddd.size(); i++) {
			ddd.get(i).reg.fationSet(home.faction());
			k++;
		}
		return k;
	}

	private final Treaty treaty = new Treaty();
	
	private final class Treaty extends WTREATY {

		private Faction f;
		
		@Override
		public boolean can(int fx, int fy, int tx, int ty, double dist) {
			return test(WORLD.REGIONS().map.get(fx, fy)) && test(WORLD.REGIONS().map.get(tx, ty));
			
		}
		
		private boolean test(Region r) {
			if (r == null)
				return true;
			if (r.faction() == f)
				return true;
			if (r.faction() == null)
				return true;
			return false;
		}
		
	}
	

	
}
