package world.regions.data;

import game.boosting.*;
import init.D;
import init.sprite.UI.UI;
import snake2d.util.misc.ACTION;
import util.data.INT_O.INT_OE;
import util.dic.DicMisc;
import world.regions.Region;
import world.regions.data.RD.RDInit;
import world.regions.data.pop.RDRace;

public class RDTax {

	private static CharSequence ¤¤taxes = "¤Taxes";
	private static CharSequence ¤¤taxRate = "¤Tax Rate";
	private static CharSequence ¤¤taxD = "¤Taxes are generated from your subjects. Higher tax rates increases taxes, but decreases loyalty.";
	
	static {
		D.ts(RDTax.class);
	}
	
	public final Boostable boost;
	public INT_OE<Region> rate;
	
	RDTax(RDInit init) {
		rate = init.count.new DataNibble(¤¤taxRate, ¤¤taxD, 10);
		
		boost = BOOSTING.push("TAX_INCOME", 0, ¤¤taxes, ¤¤taxD, UI.icons().s.money, BoostableCat.WORLD);

		init.connectable.add(new ACTION() {
			
			@Override
			public void exe() {
				
				new RBooster(new BSourceInfo(DicMisc.¤¤Base, UI.icons().s.cancel), 10, 10, false) {

					@Override
					public double get(Region t) {
						return 1.0;
					}
				}.add(boost);
				
				new RBooster(new BSourceInfo(rate.info().name, UI.icons().s.money), 0.25, 1, true) {

					@Override
					public double get(Region t) {
						return rate.getD(t);
					}
				}.add(boost);
				
				RBooster b = new RBooster(new BSourceInfo(rate.info().name, UI.icons().s.money), 0, -4, false) {

					@Override
					public double get(Region t) {
						return rate.getD(t);
					}
				};
				
				for (RDRace r : RD.RACES().all) {
					b.add(r.loyalty.target);
				}
			}
		});
		
	}


}
