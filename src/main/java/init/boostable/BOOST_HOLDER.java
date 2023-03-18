package init.boostable;

import java.util.Arrays;

import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.LIST;
import util.dic.DicMisc;
import util.gui.misc.GBox;

public interface BOOST_HOLDER {

	public CharSequence boosterName();
	public LIST<BBoost> boosts();
	
	public class BOOST_HOLDER_IMP implements BOOST_HOLDER {
		
		private CharSequence name;
		private LIST<BBoost> boosts;
		
		public BOOST_HOLDER_IMP(CharSequence name, Json json){
			this.name = name;
			boosts = BOOSTABLES.boosts(json);
		}
		
		@Override
		public CharSequence boosterName() {
			return name;
		}

		@Override
		public LIST<BBoost> boosts() {
			return boosts;
		}
	
	}
	
	public class BOOST_HOLDERCOLL implements BOOST_HOLDER {
		
		protected double[] add = new double[BOOSTABLES.all().size()];
		protected double[] mul = new double[BOOSTABLES.all().size()];
		
		private final CharSequence name;
		private final LIST<BBoost> boosts;
		
		public BOOST_HOLDERCOLL(CharSequence name, Json json){
			this.name = name;
			boosts = BOOSTABLES.boosts(json);
			Arrays.fill(add, 0);
			Arrays.fill(mul, 1);
			for (BBoost b : boosts)
				if (b.isMul())
					mul[b.boostable.index()] = b.value();
				else
					add[b.boostable.index()] = b.value();
			
		}
		
		@Override
		public CharSequence boosterName() {
			return name;
		}

		@Override
		public LIST<BBoost> boosts() {
			return boosts;
		}
	
		public double add(BOOSTABLE b) {
			return add[b.index];
		}
		
		public double mul(BOOSTABLE b) {
			return mul[b.index];
		}
		
	}
	
	public static void hover(GUI_BOX box, LIST<BBoost> boosts) {
		GBox b = (GBox) box;
		b.NL();
		b.textLL(DicMisc.¤¤Boosts);
		b.NL();
		for (BBoost bb : boosts) {
			bb.hover(b);
			b.NL();
		}
		
	}
	
	public default void hover(GUI_BOX box) {
		hover(box, boosts());
	}
	
	
}
