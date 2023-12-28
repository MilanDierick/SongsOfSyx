package game.faction.player.trade;

import game.time.TIME;
import util.dic.DicRes;
import util.info.INFO;
import util.statistics.HistoryResource;

public class PTrade {

	public final HistoryResource pricesBuy = new HistoryResource(
			new INFO(DicRes.¤¤sellPrice, ""),
			32, TIME.seasons(), true);
	public final HistoryResource pricesSell = new HistoryResource(
			new INFO(DicRes.¤¤buyPrice, ""),
			32, TIME.seasons(), true);

	PTrade(){
		
	}
}
