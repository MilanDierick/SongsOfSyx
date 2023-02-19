package init.race;

import java.io.IOException;

import game.tourism.TourismRace;
import init.paths.PATH;
import init.race.appearence.RAppearence;
import init.race.home.RaceHome;
import init.resources.*;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sprite.TILE_SHEET;
import util.keymap.KEY_COLLECTION;

public class Race implements INDEXED{
	
	private RaceBoosts bonuses;
	private RacePreferrence pref;
	public final RaceInfo info;
	public Physics physics;
	public final int index;
	public final boolean playable;
	public final String key;
	public final RaceProp behaviour;
	private RaceStats data;
	private RaceServiceSorter service;
	private RacePopulation population;
	private RaceHome home;
	private Bio bio;
	TourismRace tourism;
	
	private RAppearence appearance;
	
	private LIST<RES_AMOUNT> resources;
	
	public Race(String key, Json data, Json text, ArrayList<Race> list){
				
		this.key = key;
		index = list.add(this);
		
		info = new RaceInfo(data, text);
		playable = data.bool("PLAYABLE");
		population = new RacePopulation(data);
		behaviour = new RaceProp(data);
	}
	
	void expand(Json data, Json text, PATH sg, KeyMap<RAppearence> appearances, KeyMap<TILE_SHEET> skelleton, KeyMap<init.race.appearence.RaceSheet> children,  KeyMap<init.race.appearence.ExtraSprite> sprites, KeyMap<String[]> names) throws IOException {
		physics = new Physics(data);
		appearance = new RAppearence(this, sg, data, skelleton, appearances, children, sprites, names, physics.hitBoxsize());
		pref = new RacePreferrence(data);
		
		bonuses = new RaceBoosts(this, data);
		
		this.data = new RaceStats(this, data);
		service = new RaceServiceSorter(this);
		
		
		double[] ds = KEY_COLLECTION.fill(RESOURCES.map(), data, 100000);
		ArrayList<RES_AMOUNT> resources = new ArrayList<>(ds.length);
		for (RESOURCE r : RESOURCES.ALL()) {
			if (ds[r.index()] > 0) {
				resources.add(new RES_AMOUNT.Imp(r, (int)ds[r.index()]));
			}
		}
		this.resources = new ArrayList<>(resources);
		this.home = new RaceHome(data.value("HOME"));
		bio = new Bio(data, this);
		tourism = new TourismRace(data, this);
	}
	
	public RAppearence appearance() {
		return appearance;
	}
	
	public RacePreferrence pref() {
		return pref;
	}
	
	public RaceBoosts bonus() {
		return bonuses;
	}
	
	public RaceStats stats() {
		return data;
	}
	
	public RaceServiceSorter service() {
		return service;
	}
	
	public RacePopulation population() {
		return population;
	}
	
	public RaceHome home() {
		return home;
	}

	public Bio bio() {
		return bio;
	}
	
	@Override
	public String toString() {
		return ""+info.name + "#" + index;
	}
	
	public LIST<RES_AMOUNT> resources(){
		if (resources == null) {
			
		}
		return resources;
	}

	@Override
	public int index() {
		return index;
	}
	
	public TourismRace tourism() {
		return tourism;
	}
	
}
