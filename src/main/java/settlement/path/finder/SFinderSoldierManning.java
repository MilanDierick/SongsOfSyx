package settlement.path.finder;

import static settlement.main.SETT.*;

import settlement.army.Army;
import settlement.entity.humanoid.Humanoid;
import settlement.misc.util.FINDABLE;
import settlement.room.main.Room;
import snake2d.util.datatypes.DIR;

public final class SFinderSoldierManning extends SFinderFindable{
	
	private final Army army;
	
	SFinderSoldierManning(Army army) {
		super("s_manning");
		this.army = army;
		new TestPath(name, this);
	}

	@Override
	public FINDABLE_MANNING getReservable(int x, int y) {
		Room i = ROOMS().map.get(x, y);
		if (i == null || !(i instanceof FINDABLE_MANNING_INSTANCE))
			return null;
		FINDABLE_MANNING f = ((FINDABLE_MANNING_INSTANCE)i).getManning(x, y);
		if (f != null && f.army() == army && f.findableReservedCanBe())
			return f;
		return null;
	}

	@Override
	public FINDABLE_MANNING getReserved(int x, int y) {
		Room i = ROOMS().map.get(x, y);
		if (i == null || !(i instanceof FINDABLE_MANNING_INSTANCE))
			return null;
		FINDABLE_MANNING f = ((FINDABLE_MANNING_INSTANCE)i).getManning(x, y);
		if (f != null && f.army() == army && f.findableReservedIs())
			return f;
		return null;
	}
	
	public interface FINDABLE_MANNING extends FINDABLE {
		public DIR faceDIR();
		public void work(double time, Humanoid a);
		public boolean needsWork();
		public Army army();
	}
	
	public interface FINDABLE_MANNING_INSTANCE {
		
		public FINDABLE_MANNING getManning(int tx, int ty);
		
		
	}

}

