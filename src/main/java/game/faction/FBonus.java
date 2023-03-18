package game.faction;

import init.boostable.BOOST_LOOKUP;
import snake2d.util.sets.LIST;
import util.dic.DicMisc;

public interface FBonus extends BOOST_LOOKUP.SIMPLE {

	public LIST<BOOST_LOOKUP.SIMPLE> subs();

	@Override
	public default CharSequence name() {
		return DicMisc.¤¤Faction;
	}


}
