package world.army;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import game.faction.FACTIONS;
import game.faction.Faction;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayListShort;
import snake2d.util.sets.LIST;
import world.World;
import world.entity.army.WArmy;

public class FactionArmies {

	public final static int MAX = 64;
	
	final ArrayListShort armies = new ArrayListShort(MAX);
	private final int factionI;
	private final List list = new List();
	final int[] data;
	
	public FactionArmies(int factionI) {
		this.factionI = factionI;
		data = new int[WARMYD.dataT.intCount()];
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save (FilePutter f) {
			armies.save(f);
			f.is(data);
		}
		
		@Override	
		public void load (FileGetter f) throws IOException {
			armies.load(f);
			f.is(data);
		}
		
		@Override	
		public void clear(){
			armies.clear();
			Arrays.fill(data, 0);
		}
	};
	
	public Faction faction() {
		if (factionI == -1)
			return null;
		return FACTIONS.all().get(factionI);
	}
	
	public LIST<WArmy> all(){
		return list;
	}
	
	public boolean canCreate() {
		return World.ENTITIES().armies.canCreate() && armies.hasRoom();
	}
	
	public WArmy create(int tx, int ty) {
		if (!canCreate())
			throw new RuntimeException("" + armies.size());
		World.ENTITIES().armies.create(tx, ty, faction());
		return list.get(list.size()-1);
	}
	
	void recount() {
		
	}
	
	private final class List implements LIST<WArmy>, Iterator<WArmy>{


		private int ii;
		
		@Override
		public Iterator<WArmy> iterator() {
			ii = 0;
			return this;
		}

		@Override
		public WArmy get(int index) {
			return World.ENTITIES().armies.get(armies.get(index));
		}

		@Override
		public boolean contains(int i) {
			return i >= 0 && i < armies.size();
		}

		@Override
		public boolean contains(WArmy object) {
			for (int i = 0; i < armies.size(); i++) {
				if (get(i) == object)
					return true;
			}
			return false;
		}

		@Override
		public int size() {
			return armies.size();
		}

		@Override
		public boolean isEmpty() {
			return armies.isEmpty();
		}

		@Override
		public boolean hasNext() {
			return ii < armies.size();
		}

		@Override
		public WArmy next() {
			WArmy r = get(ii);
			ii++;
			return r;
		}
		
	}
	
}
