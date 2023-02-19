package game.faction;

import java.io.IOException;

import game.time.TIME;
import game.time.TIMECYCLE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.statistics.HistoryRace;
import world.map.regions.Region;

public class FCapitol extends FactionResource{

	public final static int SAVED = 32;
	
	private static TIMECYCLE time() {
		return TIME.years();
	}
	
	public final HistoryRace population = new HistoryRace(SAVED*2, time(), true);
	
	private final Faction f;
	
	public FCapitol(Faction f) {
		this.f = f;
	}
	
	@Override
	protected void save(FilePutter file) {
		population.save(file);
	}


	@Override
	protected void load(FileGetter file) throws IOException {
		population.load(file);
	}


	@Override
	protected void clear() {
		population.clear();
	}

	@Override
	protected void update(double ds) {
		// TODO Auto-generated method stub
		
	}
	
	public final Region r() {
		return f.kingdom().realm().capitol();
	}

	
	
}
