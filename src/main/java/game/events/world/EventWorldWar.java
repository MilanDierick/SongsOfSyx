package game.events.world;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import world.WORLD;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;
import world.regions.data.RD;
import world.regions.data.pop.RDRace;

public class EventWorldWar extends EventResource{

	
	private static final double dtime = TIME.secondsPerDay*16;
	private double timer = dtime;
	private int nextFaction;
	
	EventWorldWar(){
		
	}
	
	@Override
	protected void update(double ds) {
		timer -= ds;
		if (timer > 0)
			return;
		
		FactionNPC f = FACTIONS.NPCs().getC(nextFaction);
		clear();
		timer = 16;
		if (f != null && FACTIONS.DIP().war.getEnemies(f).size() == 0) {
			
			Faction enemy = null; 
			double bestE = 0;
			
			for (RDist d : WORLD.PATH().tmpRegs.all(f.capitolRegion(), WTREATY.NEIGHBOURSF(f), WRegSel.CAPITOLS(f))) {
				if (d.reg.faction() == f || d.reg.faction() == FACTIONS.player())
					continue;
				if (FACTIONS.player().realm().all().size() <= 1 && RD.DIST().factionBordersPlayer(d.reg.faction()))
					continue;
				double v = 0;
				for (RDRace race : RD.RACES().all) {
					v += race.pop.get(d.reg)*race.race.pref().race(f.race());
					
				}
				v = 1.0/v;
				if (v > bestE) {
					enemy = d.reg.faction();
					bestE = v;
				}
			}
			
			if (enemy != null) {
				FACTIONS.DIP().war.set(f, enemy, true);
				timer += TIME.secondsPerDay*20;
			}else {
				
			}
				
		}
		
		
	}


	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.i(nextFaction);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		nextFaction = file.i();
		timer = 16;
	}

	@Override
	protected void clear() {
		timer = dtime;
		nextFaction = RND.rInt();
	}	

}
