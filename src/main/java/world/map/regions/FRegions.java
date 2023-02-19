package world.map.regions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.race.Race;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayListShort;
import snake2d.util.sets.LIST;
import util.data.INT;
import util.data.INT.IntImp;
import util.statistics.HISTORY_COLLECTION;
import util.statistics.HistoryRace;
import world.World;

public final class FRegions {
	
	final ArrayListShort regions = new ArrayListShort(128);
	short capitolI = -1;
	
	private final int faction;
	final HistoryRace population = new HistoryRace(16, TIME.years(), true);
	final IntImp admin = new IntImp();
	private final List list = new List();
	final int[] data;
	
	public FRegions(RegionInit init, int faction){
		this.faction = faction;
		this.data = new int[init.realmcount.intCount()];
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save (FilePutter f) {
			regions.save(f);
			population.save(f);
			admin.save(f);
			f.s(capitolI);
			f.is(data);
		}
		
		@Override	
		public void load (FileGetter f) throws IOException {
			regions.load(f);
			population.load(f);
			admin.load(f);
			capitolI = f.s();
			f.is(data);
		}
		
		@Override	
		public void clear(){
			regions.clear();
			population.clear();
			admin.clear();
			capitolI = -1;
			Arrays.fill(data, 0);
		}
	};
	
	void recount() {
		int points = 0;
		for (Region r : list) {
			points +=  REGIOND.OWNER().adminCostAll(r);
		}
		admin.set(points);
	}
	
	public Faction faction() {
		return FACTIONS.getByIndex(faction);
	}
	
	public HISTORY_COLLECTION<Race> population() {
		return population;
	}
	
	public INT spentAdmin() {
		return admin;
	}
	
	public LIST<Region> regions(){
		return list;
	}
	
	public Region capitol() {
		if (capitolI == -1)
			return null;
		return World.REGIONS().getByIndex(capitolI);
	}

	
	private class List implements LIST<Region>, Iterator<Region>{

		private int ii;
		
		@Override
		public Iterator<Region> iterator() {
			ii = 0;
			return this;
		}

		@Override
		public Region get(int index) {
			return World.REGIONS().getByIndex(regions.get(index));
		}

		@Override
		public boolean contains(int i) {
			return i >= 0 && i < regions.size();
		}

		@Override
		public boolean contains(Region object) {
			for (int i = 0; i < regions.size(); i++) {
				if (get(i) == object)
					return true;
			}
			return false;
		}

		@Override
		public int size() {
			return regions.size();
		}

		@Override
		public boolean isEmpty() {
			return regions.isEmpty();
		}

		@Override
		public boolean hasNext() {
			return ii < regions.size();
		}

		@Override
		public Region next() {
			Region r = get(ii);
			ii++;
			return r;
		}
		
	}



	
}
