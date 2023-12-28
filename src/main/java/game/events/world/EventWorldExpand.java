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
import world.army.AD;
import world.entity.army.WArmy;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;
import world.regions.Region;
import world.regions.data.RD;

public class EventWorldExpand extends EventResource{

	private static final double dtime = TIME.secondsPerDay*2;
	private double timer = dtime;
	private int nextFaction = RND.rInt(FACTIONS.MAX);
	
	
	EventWorldExpand(){
		
	}
	
	@Override
	protected void update(double ds) {
		timer -= ds;
		if (timer > 0)
			return;
		
		Faction f = FACTIONS.getByIndex(nextFaction);
		if (f != null && f.isActive() && f instanceof FactionNPC) {
			trigger((FactionNPC) f);
			
		}
		clear();
	}

	public boolean trigger(FactionNPC f) {
		
		
		if (FACTIONS.DIP().war.getEnemies(f).size() > 0)
			return false;
		
		Region best = null;
		double bv = 0;
		
		for (RDist d : WORLD.PATH().tmpRegs.all(f.capitolRegion(), WTREATY.NEIGHBOURSF(f), WRegSel.FACTION(null))){
			double v = RD.OWNER().prevOwner(d.reg) == f ? 5.0 : 1.0;
			v /= d.distance;
			if (v > bv) {
				bv = v;
				best = d.reg;
			}
		}
		
		if (best == null)
			return false;
		
		for (WArmy a : f.armies().all()) {
			if (AD.power().get(a) > RD.MILITARY().power.getD(best)*1.5) {
				a.besiege(best);
				return true;
			}
		}
		
		return false;
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
	}

	@Override
	protected void clear() {
		timer = RND.rFloat()*dtime;
		nextFaction = RND.rInt(FACTIONS.MAX);
	}	

}
