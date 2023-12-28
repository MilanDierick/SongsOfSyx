package world.battle;

import java.io.IOException;

import snake2d.util.file.*;
import snake2d.util.sets.ArrayListInt;
import snake2d.util.sets.Bitmap1D;
import world.WORLD;
import world.entity.army.WArmy;
import world.entity.army.WArmyConstructor;

final class PollBattles implements SAVABLE{

	private final Bitmap1D map = new Bitmap1D(WArmyConstructor.MAX, false);
	private final ArrayListInt current = new ArrayListInt(WArmyConstructor.MAX);
	
	public PollBattles() {

	}
	
	@Override
	public void save(FilePutter file) {
		map.save(file);
		current.save(file);
	}
	@Override
	public void load(FileGetter file) throws IOException {
		map.load(file);
		current.load(file);
	}
	@Override
	public void clear() {
		map.clear();
		current.clear();
	}


	
	public void add(WArmy a) {
		
		if (map.get(a.armyIndex()))
			return;
		
		map.set(a.armyIndex(), true);
		current.add(a.armyIndex());
		
	}

	public WArmy poll() {
		while(!current.isEmpty()) {
			int ai = current.get(current.size()-1);
			return WORLD.ENTITIES().armies.get(ai);
		}
		return null;
	}
	
	public void skip() {
		map.set(current.get(current.size()-1), false);
		current.remove(current.size()-1);
	}

	
}
