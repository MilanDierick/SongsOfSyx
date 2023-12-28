package world.regions.data;

import game.GAME;
import game.boosting.*;
import init.sprite.UI.Icon;
import snake2d.util.misc.CLAMP;
import util.data.INT_O.INT_OE;
import world.regions.Region;
import world.regions.data.RD.RDInit;

public class RDBoostCache {

	public final Boostable boost;
	public final INT_OE<Region> cache;
	public final INT_OE<Region> value;
	
	public RDBoostCache(RDInit init, String key, CharSequence name, CharSequence desc, Icon icon) {
		this(init, BOOSTING.push(key, 0, name, desc, icon, BoostableCat.WORLD_DUMP));
	}
	
	public RDBoostCache(RDInit init, Boostable boost) {
		this.boost = boost;
		cache = init.count.new DataBit();
		value = init.count.new DataNibble();
	}
	
	public double get(Region reg) {
		if (reg == null)
			return 0;
		int upI = ((GAME.updateI()>>5) & 1);
		if (cache.get(reg) != upI) {
			cache.set(reg, upI);
			
			double v = pget(reg);
			value.setD(reg, v);
		}
		return value.getD(reg);
	}
	
	protected double pget(Region reg) {
		double v = boost.get(reg);
		v = CLAMP.d(v, 0, 1);
		return v;
	}
	
}
