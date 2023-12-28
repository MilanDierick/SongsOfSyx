package settlement.army;

import init.D;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.DataOL;
import util.data.INT_O.INT_OE;
import util.dic.DicArmy;
import util.info.INFO;

public final class ArmyMorale {

	private static CharSequence ¤¤numbers = "¤Numbers";
	private static CharSequence ¤¤numbersD = "¤The size of your army compared to the size of the enemy";
	
	static {
		D.ts(ArmyMorale.class);
	}
	
	final static DataOL<Army> data = new DataOL<Army>() {
		@Override
		protected long[] data(Army t) {
			return t.moraleData;
		}
	};
	
	public static final INT_OE<Army> CASULTIES = data.new DataInt(DivMorale.CASULTIES.info()) {
		@Override
		public double getD(Army t) {
			double men = t.men();
			if (men == 0)
				return get(t) > 0 ? 0 : 1;
			double r = CLAMP.d(1.0-0.25*get(t)/men, 0.5, 1);
			return r;
		};
	};
	public static final INT_OE<Army> DESERTION = data.new DataInt(DivMorale.DESERTION.info()) {
		@Override
		public double getD(Army t) {
			double men = t.men();
			if (men == 0)
				return get(t) > 0 ? 0 : 1;
			double r = CLAMP.d(1.0-0.4*get(t)/men, 0, 1);
			return r;
		};
	};
	public static final DOUBLE_O<Army> NUMBERS = new DOUBLE_O<Army>() {

		private final INFO info = new INFO(¤¤numbers, ¤¤numbersD);
		
		@Override
		public double getD(Army t) {
			if (t.men() == 0) {
				return t.enemy().men() > 0 ? 0.5 : 1;
			}
			if (t.enemy().men() == 0) {
				return 1;
			}
			
			double bon = (double)t.men()/t.enemy().men();
			return CLAMP.d(bon, 0.75, 1.5);
		}
		
		@Override
		public  INFO info() {
			return info;
		};
	
	};
	public static final DOUBLE_OE<Army> SUPPLIES = data.new DataDouble(new INFO(DicArmy.¤¤Supplies, DicArmy.¤¤SuppliesD));

	
	public static final LIST<DOUBLE_O<Army>> factors = new ArrayList<DOUBLE_O<Army>>(CASULTIES, DESERTION, NUMBERS, SUPPLIES);
	public static final LIST<DOUBLE_OE<Army>> resetable = new ArrayList<DOUBLE_OE<Army>>(CASULTIES, DESERTION, SUPPLIES);
}
