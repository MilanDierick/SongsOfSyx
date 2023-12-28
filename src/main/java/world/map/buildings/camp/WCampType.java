package world.map.buildings.camp;

import java.io.IOException;

import game.faction.Faction;
import game.values.GVALUES;
import game.values.Lockable;
import init.biomes.CLIMATES;
import init.biomes.TERRAINS;
import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import init.race.RACES;
import init.race.Race;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sprite.TILE_SHEET;
import util.keymap.KEY_COLLECTION;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public final class WCampType implements INDEXED{

	private final int index;
	public final TILE_SHEET sheet;
	public final COLOR cMask;
	public final LIST<CharSequence> names;
	public final Race race;
	public final int popFrom;
	public final int popTo;
	public final double replenishMin;
	public final double replenishMax;
	public final Lockable<Faction> reqs;
	
	
	public final double[] climates;
	public final double[] terrains;

	
	WCampType(String key, LISTE<WCampType> all, Json jdata, Json jtext, TILE_SHEET sheet) throws IOException{
		index = all.add(this);
		this.sheet = sheet;
		cMask = new ColorImp(jdata, "COLOR_MASK");
		names = new ArrayList<>(jtext.texts("NAMES", 1, 500));
		race = RACES.map().get(jdata.value("RACE"), jdata);
		climates = KEY_COLLECTION.fill(CLIMATES.MAP(), jdata, 1);
		terrains = KEY_COLLECTION.fill(TERRAINS.MAP(), jdata, 100);
		reqs = GVALUES.FACTION.LOCK.push("WORLD_CAMP_"+key, race.info.names, race.info.names, race.appearance().icon);
		reqs.push(jdata);
		popFrom = jdata.i("CAMP_SIZE_FROM", 1, 1000);
		popTo = jdata.i("CAMP_SIZE_TO", popFrom, 1000);
		
		replenishMin = jdata.d("REPLENISH_PER_DAY_FROM");
		replenishMax = jdata.d("REPLENISH_PER_DAY_TO");
		
	}
	
	static LIST<WCampType> types() throws IOException{
		
		LinkedList<WCampType> all = new LinkedList<>();
		KeyMap<TILE_SHEET> sheets = new KeyMap<>();
		
		ResFolder f = PATHS.RACE().folder("worldcamp");
		
		for (String file : f.init.getFiles()) {
			
			Json jdata = new Json(f.init.get(file));
			Json jtext = new Json(f.text.get(file));
			
			String ssprite = jdata.value("SPRITE");
			TILE_SHEET sheet = sheets.get(ssprite);
			if (sheet == null) {
				TILE_SHEET s = new ITileSheet(f.sprite.get(ssprite), 132, 126) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.singles.init(0, 0, 1, 1, 2, 4, d.s24);
						s.singles.paste(3, true);
						return d.s24.saveGame();
					}
				}.get();
				sheets.put(ssprite, s);
				sheet = s;
			}
			new WCampType(file, all, jdata, jtext, sheet);
		}
		
		return new ArrayList<>(all);
		
	}
	
	@Override
	public int index() {
		return index;
	}
	

	

	
}
