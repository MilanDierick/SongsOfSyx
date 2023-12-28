package world.army;

import game.faction.Faction;
import init.resources.RESOURCE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.INDEXED;
import world.army.AD.*;
import world.entity.army.WArmy;

public class ADSupply implements INDEXED {

	public final RESOURCE res;
	final Imp current;
	final Imp needed;
	final Imp target;
	
	public final int minimumPerMan;
	public final double morale;
	public final double health;
	public final double usedPerDay;
	private final int index;

	ADSupply(int index, ADInit init, RESOURCE res, double consumption, int equipPerman, double morale, double health) {
		this.usedPerDay = consumption;
		this.index = index;
		current = new Imp(init, init.dataA.new DataInt(), res.names, res.names) {
			@Override
			public void set(WArmy t, int i) {
				AD.power().clearCache(t);
				super.set(t, i);
			}
		};
		needed = new Imp(init, init.dataA.new DataInt(), res.names, res.names);
		target = new Imp(init, init.dataA.new DataInt(), res.names, res.names);
		
		this.res = res;
		this.minimumPerMan = equipPerman;
		this.morale = morale;
		this.health = health;
	}
	
	public int max(WArmy a) {
		return max(target.get(a));
	}
	
	int max(int amount) {
		return (int) (amount + 24.0*amount*usedPerDay);
	}
	
	public int needed(WArmy a) {
		return CLAMP.i(max(a) - current.get(a), 0, Integer.MAX_VALUE);
	}
	
	public int needed(Faction faction) {
		int am = 0;
		for (WArmy a : faction.armies().all())
			am += needed(a);
		return am;
	}

	@Override
	public int index() {
		return index;
	}
	
	public WArmyDataE current() {
		return current;
	}
	
	public WArmyData used() {
		return needed;
	}
	
	public WArmyData target() {
		return target;
	}
	
	public double morale(WArmy a) {
		return CLAMP.d(1 - morale * (used().get(a) - current().get(a))/used().get(a), 0, 1);
	}
	
	public double health(WArmy a) {
		return CLAMP.d(1 - health * (used().get(a) - current().get(a))/used().get(a), 0, 1);
	}

	public double getD(WArmy a) {
		double t = target.get(a);
		if (t <= 0)
			return 0;
		return CLAMP.d(current.get(a)/t, 0, 1);
	}

}