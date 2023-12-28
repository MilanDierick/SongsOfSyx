package init.need;

import game.boosting.BoostableCat;
import init.D;
import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import snake2d.util.sets.*;
import util.keymap.RCollection;

public class NEEDS {

	private static NEEDS self;

	{
		D.gInit(this);
	}

	private final BoostableCat bCat = new BoostableCat("RATES_", D.g("rates"), "", BoostableCat.TYPE_SETT);
	private final ArrayListGrower<NEED> ALL = new ArrayListGrower<>();
	private final RCollection<NEED> coll;
	private final Types types;
	private final ResFolder f = PATHS.STATS().folder("need");

	public NEEDS() {
		self = this;


		KeyMap<NEED> map = new KeyMap<>();
		ResFolder f = PATHS.STATS().folder("need");

		for (String k : f.init.getFiles()) {
			NEED n = new NEED(k, f, ALL, bCat, true);
			map.put(k, n);
		}
		types = new Types();
		
		for (NEED n : ALL)
			map.put(n.key, n);
		map.expand();
		
		coll = new RCollection<NEED>("NEED", map) {

			@Override
			public NEED getAt(int index) {
				return ALL.get(index);
			}

			@Override
			public LIST<NEED> all() {
				return ALL;
			}
		};
		
	}

	public static LIST<NEED> ALL() {
		return self.ALL;
	}

	public static BoostableCat bCat() {
		return self.bCat;
	}

	public static Types TYPES() {
		return self.types;
	}

	public static RCollection<NEED> MAP() {
		return self.coll;
	}

	public final class Types {
		public final NEED HUNGER = new NEED("_HUNGER", f, ALL, bCat, false);
		public final NEED THIRST = new NEED("_THIRST", f, ALL, bCat, true);
		public final NEED SKINNYDIP = new NEED("_SKINNYDIP", f, ALL, bCat, true);
		public final NEED TEMPLE = new NEED("_TEMPLE", f, ALL, bCat, true);
		public final NEED SHRINE = new NEED("_SHRINE", f, ALL, bCat, true);
		public final NEED SHOPPING = new NEED("_SHOPPING", f, ALL, bCat, true);
	}

}
