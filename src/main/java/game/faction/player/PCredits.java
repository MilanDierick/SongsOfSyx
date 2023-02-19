package game.faction.player;

import game.faction.FACTIONS;
import game.faction.FCredits;
import game.time.TIME;
import init.D;
import init.boostable.BOOSTABLES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.HCLASS;
import snake2d.util.color.ColorImp;
import snake2d.util.misc.CLAMP;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.info.INFO;
import util.statistics.HistoryResource;

public final class PCredits extends FCredits{
	
	{D.gInit(this);}

	public final HistoryResource pricesBuy = new HistoryResource(
			new INFO(D.g("Sell-Price"), D.g("sellPriceD", "What price the highest bidder buys a ware for.")),
			16, TIME.seasons(), true);
	public final HistoryResource pricesSell = new HistoryResource(
			new INFO(D.g("Buy-Price"), D.g("buyPriceD", "The lowest price at which a faction will sell this ware.")),
			16, TIME.seasons(), true);
	
	public final CredHistory mercinaries = new CredHistory(DicArmy.¤¤Mercenaries , D.g("MercenaryD", "Mercenaries can be conscripted into your armies and cost credits to upkeep each day."), new ColorImp(94, 36, 26));
	public final CredHistory tourists = new CredHistory(DicMisc.¤¤Tourists , D.g("TouristsD", "Tourists that visit your city will give you some money at the end of their stay."), new ColorImp(80, 80, 127));
	public final CredHistory handouts = new CredHistory(D.g("Handouts") , D.g("HandoutsD", "Money that has been distributed amongst the people."), new ColorImp(80, 80, 127));
	
	public PCredits() {
		super(48, TIME.seasons());
		saves = saves.join(pricesBuy, pricesSell, mercinaries.saver, tourists.saver, handouts.saver);
		all = all.join(mercinaries).join(tourists).join(handouts);
	}
	
	private double tt = -1;
	
	@Override
	protected void update(double ds) {
		int t = (int) tt;
		tt += ds/16.0;
		int am = (int) tt - t;
		while (am-- > 0) {
			if (tt >= RESOURCES.ALL().size())
				tt = 0;
			RESOURCE r = RESOURCES.ALL().get((int)(tt));
			pricesSell.set(r, FACTIONS.tradeUtil().getBuyPriceBest(FACTIONS.player(), r));
			pricesBuy.set(r, FACTIONS.tradeUtil().getSellPriceBest(FACTIONS.player(), r));
			tt++;
		}		
		super.update(ds);
	}
	
	public double tradePenalty(double price) {
		double b = CLAMP.d(1.0/BOOSTABLES.CIVICS().TRADE.get(HCLASS.CITIZEN, null), 0, 10000);
		return  CLAMP.d((price*0.2 + price*0.7*b), 0, price);
	}
	
}