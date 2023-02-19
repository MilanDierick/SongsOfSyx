package init.race.home;

import java.io.IOException;

import init.paths.PATHS;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import snake2d.util.file.Json;

public final class RaceHome {

	private final RaceHomeClass DUMMY;
	private final RaceHomeClass[] all = new RaceHomeClass[HCLASS.ALL.size()];
	
	
	public RaceHome(String key) throws IOException{
		
		Json json = new Json(PATHS.INIT().getFolder("race").getFolder("home").get(key));
		DUMMY = new RaceHomeClass();
		for (int i = 0; i < all.length; i++)
			all[i] = DUMMY;
		all[HCLASS.CITIZEN.index()] = new RaceHomeClass(json.json(HCLASS.CITIZEN.key));
		all[HCLASS.NOBLE.index()] = new RaceHomeClass(json.json(HCLASS.NOBLE.key));
		all[HCLASS.SLAVE.index()] = new RaceHomeClass(json.json(HCLASS.SLAVE.key));
	}
	
	public RaceHomeClass clas(Humanoid h) {
		if (h == null)
			return DUMMY;
		return all[h.indu().clas().index()];
	}
	
	public RaceHomeClass clas(Induvidual h) {
		return all[h.clas().index()];
	}
	
	public RaceHomeClass clas(HCLASS c) {
		return all[c.index()];
	}
	
	
}
