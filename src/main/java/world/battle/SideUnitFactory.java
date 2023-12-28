package world.battle;

import init.config.Config;
import snake2d.util.sets.ArrayList;

class SideUnitFactory {
	
	public final Side A = new Side(this);
	public final Side B = new Side(this);
	
	private final ArrayList<SideUnit> all = new ArrayList<SideUnit>(Config.BATTLE.DIVISIONS_PER_ARMY*2);
	private int i;
	
	public SideUnitFactory() {
		while(all.hasRoom())
			all.add(new SideUnit());
	}
	
	public SideUnit next() {
		int ii = i;
		i++;
		return all.get(ii);
	}
	
	void clear() {
		i = 0;
		A.clear();
		B.clear();
	}
	
}