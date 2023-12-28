package settlement.entity.animal.spawning;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import settlement.entity.animal.ANIMALS;
import settlement.entity.animal.AnimalSpecies;
import settlement.main.CapitolArea;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.updating.IUpdater;

public final class AnimalSpawning {

	private final LIST<AnimalSpawnSpot> spots;
	public static final double SPAWN_RATE_DAY = 1.0/100;
	private final IUpdater uper = new IUpdater(16, TIME.secondsPerDay) {
		@Override
		protected void update(int i, double timeSinceLast) {
			AnimalSpawnSpot sp = spots.get(i);
			if (sp.active())
				max -= sp.max();
			sp.update(SPAWN_RATE_DAY);
			if (sp.active())
				max += sp.max();
		}
	};
	private int max = 0;
	
	private final double[] killed;
	
	
	public AnimalSpawning(ANIMALS animals) {
		ArrayList<AnimalSpawnSpot> li = new ArrayList<>(16);
		for (int i = 0; i < li.max(); i++)
			li.add(new AnimalSpawnSpot(i));
		this.spots = li;
		killed = new double[animals.species.all().size()];
	}
	
	public void update(float ds) {
		uper.update(ds);
	}
	
	public int spawnsPerDay() {
		return (int) Math.ceil(max*SPAWN_RATE_DAY);
	}
	
	public void generate(ANIMALS animals, CapitolArea carea) {
		saver.clear();
		new Generator(animals, carea, spots);
		for (AnimalSpawnSpot s : spots)
			max += s.max();
	}
	
	
	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			for (AnimalSpawnSpot s : spots)
				s.save(file);
			uper.save(file);
			file.dsE(killed);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			clear();
			for (AnimalSpawnSpot s : spots) {
				s.load(file);
				max += s.max();
			}
			uper.load(file);
			file.dsE(killed);
		}
		
		@Override
		public void clear() {
			for (AnimalSpawnSpot s : spots)
				s.clear();
			uper.clear();
			max = 0;
			Arrays.fill(killed, 0.0);
		}
	};
	
	public LIST<AnimalSpawnSpot> all(){
		return spots;
	}
	
	public boolean isTimeForAKill(AnimalSpecies s) {
		return killed[s.index()]/4 >= 1;
	}
	
	public void reportKilled(AnimalSpecies s) {
		killed[s.index()] += s.danger;
	}
	
	public void reportKillRevenge(AnimalSpecies s) {
		killed[s.index()] -= 2;
	}
	
}
