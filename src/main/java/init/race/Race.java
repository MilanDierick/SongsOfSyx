package init.race;

import java.io.IOException;

import game.boosting.*;
import game.tourism.TourismRace;
import init.race.appearence.RAppearence;
import init.race.home.RaceHome;
import init.resources.*;
import init.sprite.UI.Icon;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.keymap.KEY_COLLECTION;

public class Race implements INDEXED, BOOSTABLE_O{
	
	public final BoostSpecs boosts;
//	private RaceBoosts bonuses;
	private RacePreferrence pref;
	public final RaceInfo info;
	public Physics physics;
	public final int index;
	public final boolean playable;
	public final String key;
	public final RaceProp behaviour;
	
	private KingMessage kmess;
	private RaceStats data;
	private RaceServiceSorter service;
	private RacePopulation population;
	private RaceHome home;
	private Bio bio;
	TourismRace tourism;
	
	private RAppearence appearance;
	
	private static final LIST<RES_AMOUNT> rNo = new ArrayList<RES_AMOUNT>(0);
	
	private LIST<RES_AMOUNT> resources = rNo;
	private LIST<RES_AMOUNT> resourceGroom = rNo;

	
	public Race(String key, Json data, Json text, ArrayList<Race> list){
				
		this.key = key;
		index = list.add(this);
		
		info = new RaceInfo(data, text);
		playable = data.bool("PLAYABLE");
		population = new RacePopulation(data);
		behaviour = new RaceProp(data);
		boosts = new BoostSpecs(info.names, new SPRITE.Imp(Icon.S) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				appearance.iconBig.render(r, X1, X2, Y1, Y2);
				
			}
		}, false);
		boosts.push(data, null);
	}
	

	
	void expand(ExpandInit init) throws IOException {
		
		Json data = new Json(init.p.get(key));
		physics = new Physics(data);
		appearance = new RAppearence(this, data, init, physics.hitBoxsize());
		pref = new RacePreferrence(data, this);
		
		kmess = KingMessage.make(data, init);
		
		this.data = new RaceStats(this, data);
		service = new RaceServiceSorter(this);
		
		double[] ds = KEY_COLLECTION.fill(RESOURCES.map(), data, 100000);
		ArrayList<RES_AMOUNT> resources = new ArrayList<>(ds.length);
		for (RESOURCE r : RESOURCES.ALL()) {
			if (ds[r.index()] > 0) {
				resources.add(new RES_AMOUNT.Imp(r, (int)ds[r.index()]));
			}
		}
		this.resources = resources;
		
		if (data.has("RESOURCE_GROOMING"))
			resourceGroom = RES_AMOUNT.make(data.json("RESOURCE_GROOMING"));
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
	
//	public RaceBoosts bonus() {
//		return bonuses;
//	}
	
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
		return resources;
	}
	
	public LIST<RES_AMOUNT> resourcesGroom(){
		return resourceGroom;
	}

	@Override
	public int index() {
		return index;
	}
	
	public TourismRace tourism() {
		return tourism;
	}
	
	public KingMessage kingMessage() {
		return kmess;
	}




	@Override
	public double boostableValue(Boostable bo, BValue v) {
		return v.vGet(this);
	}
	
}
