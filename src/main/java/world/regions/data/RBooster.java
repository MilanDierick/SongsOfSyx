package world.regions.data;


import game.boosting.*;
import game.faction.Faction;
import world.regions.Region;

public abstract class RBooster extends BoosterImp {

	public RBooster(BSourceInfo info, double from, double to, boolean isMul) {
		super(info, from, to, isMul);
	}

	@Override
	public double vGet(Faction f) {
		return 0;
	}
	
	@Override
	public boolean has(Class<? extends BOOSTABLE_O> b) {
		return b == Region.class;
	}
	
	protected abstract double get(Region reg);
	
	@Override
	public double vGet(Region reg) {
		return get(reg);
	}

}
