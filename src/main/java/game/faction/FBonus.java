package game.faction;

import init.boostable.BOOSTER_COLLECTION;
import snake2d.util.sets.LIST;
import util.dic.DicMisc;

public interface FBonus extends BOOSTER_COLLECTION.SIMPLE {

	public LIST<BOOSTER_COLLECTION.SIMPLE> subs();

	@Override
	public default CharSequence name() {
		return DicMisc.¤¤Faction;
	}


}
