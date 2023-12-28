package game.events.world;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import world.WORLD;
import world.regions.Region;

public class EventWorldPopup extends EventResource{

	
	private static final double dtime = TIME.secondsPerDay*16;
	private double timer = dtime;
	private int nextRegion;
	
	EventWorldPopup(){
		
	}
	
	@Override
	protected void update(double ds) {
		timer -= ds;
		if (timer > 0)
			return;
		
		if (FACTIONS.activateAvailable() == 0) {
			clear();
			return;
		}
		
		Region r = WORLD.REGIONS().active().getC(nextRegion);
		
		if (r != null && r.faction() == null) {
			FactionNPC f = FACTIONS.activateNext(r);
			f.generate(null, true);
			clear();
		}
	}


	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.i(nextRegion);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		nextRegion = file.i();
	}

	@Override
	protected void clear() {
		timer = RND.rFloat()*dtime;
		nextRegion = RND.rInt();
	}	

}
