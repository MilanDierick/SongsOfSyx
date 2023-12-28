package init.biomes;

import init.paths.PATHS;
import init.race.RACES;
import snake2d.util.file.Json;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.info.INFO;

public abstract class BUILDING_PREF implements INDEXED{
	
	public CharSequence name;
	private final int index;
	public final String key;
	public final double[] defaultPref = new double[RACES.all().size()];
	
	BUILDING_PREF(String key, LISTE<BUILDING_PREF> all) {
		this.name = new INFO(new Json(PATHS.TEXT_SETTLEMENT().getFolder("structure").get(key))).name;
		index = all.add(this);
		this.key = key;
		RACES.map().fill("PREFERENCE", defaultPref, new Json(PATHS.INIT_SETTLEMENT().getFolder("structure").get(key)), 0, 1);
	}

	@Override
	public int index() {
		return index;
	}
	
	public abstract SPRITE icon() ;
	
	
}