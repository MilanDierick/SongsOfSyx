package game;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import init.D;
import init.race.RACES;
import init.race.Race;
import menu.MapType;
import script.ScriptLoad;
import snake2d.CORE_STATE;
import snake2d.util.file.*;
import snake2d.util.sets.*;
import view.main.VIEW;

public class GameConRandom implements CORE_STATE.Constructor, SAVABLE{

	private static CharSequence ¤¤small = "¤small";
	private static CharSequence ¤¤big = "¤big";
	private static CharSequence ¤¤huge = "¤huge";
	
	private static CharSequence ¤¤nort = "¤northern";
	private static CharSequence ¤¤south = "¤southern";
	private static CharSequence ¤¤central = "¤central";
	
	private static CharSequence ¤¤startRace = "¤starting race";
	
	private static CharSequence ¤¤size = "¤size";
	private static CharSequence ¤¤latitude = "¤latitude";
	
	static {
		D.ts(GameConRandom.class);
	}
	
	public final KeyMap<Double> BOOSTS = new KeyMap<>();
	
	public LIST<ScriptLoad> scripts = new ArrayList<ScriptLoad>(0);
	
	public final LinkedList<GameConSpec> specs = new LinkedList<GameConSpec>();
	public final GameConSpec size = new GameConSpec(
			new CharSequence[] {¤¤small, ¤¤big, ¤¤huge}, 
			new double[] {16*14, 16*18, 16*22}, 
			0, ¤¤size
			);
	public final GameConSpec lat = new GameConSpec(
			new CharSequence[] {¤¤central, ¤¤nort, ¤¤south}, 
			new double[] {0.5, 1.0, 0}, 
			0, ¤¤latitude
			);
	public final GameConSpec race;
	private MapType t;
	public GameConRandom(MapType t){
		specs.add(size);
		specs.add(lat);
		this.t = t;
		int i = 0;
		for (Race ra: RACES.all())
			if (ra.playable)
				i++;
		CharSequence[] rnames = new CharSequence[i];
		double[] values = new double[i];
		i = 0;
		for (Race ra: RACES.all())
			if (ra.playable) {
				rnames[i] = ra.info.name;
				values[i++] = ra.index;
			}
		race = new GameConSpec(rnames, values, 0, ¤¤startRace);
		specs.add(race);
	}
	
	public MapType map() {
		return t;
	}
	
	public void mapTypeset(MapType t) {
		this.t = t;
	}
	
	public Collection<GameConSpec> getSpecs(){
		return specs;
	}
	
	@Override
	public CORE_STATE getState() {
		
		new GAME(this);
		CORE_STATE s = new VIEW();
		VIEW.world().viewGenerator.activate();
		return s;
	}

	@Override
	public void save(FilePutter file) {
		for (GameConSpec r : specs)
			file.i(r.index);
		t.save(file);
		
		file.i(BOOSTS.keys().size());
		for(String k : BOOSTS.keys()) {
			file.chars(k);
			file.d(BOOSTS.get(k));
		}
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		for (GameConSpec r : specs)
			r.index = file.i();
		t = new MapType(file);
		int am = file.i();
		BOOSTS.clear();
		for(int i = 0; i < am; i++) {
			BOOSTS.put(file.chars(), file.d());
		}
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	
}
