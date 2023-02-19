package settlement.room.industry.module;

import init.boostable.BOOSTABLE;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public interface INDUSTRY_HASER {

	public LIST<Industry> industries();

	static LIST<Industry> createIndustries(RoomBlueprintImp blue, RoomInitData init, RoomBoost[] boosts,
			BOOSTABLE bonus) {

		Json[] js = init.data().jsons("INDUSTRIES", 1);

		ArrayList<Industry> res = new ArrayList<>(js.length);
		for (Json j : js) {
			Industry i = new Industry(blue, j, boosts, bonus);
			if (i.outs().size() == 0)
				j.error(blue.info + " has no out resources declared. This can be due to an outdated mod.", "INDUSTRIES");
			res.add(i);
		}
		return res;

	}
	
	public default double industryFormatProductionRate(GText text, IndustryResource i, RoomInstance ins) {
		double n = IndustryUtil.calcProductionRate(i.rate, ((ROOM_PRODUCER) ins).industry(), ins);
		n*= ins.employees().employed();
		text.add('+');
		GFORMAT.fRel(text, n*ins.employees().efficiency(), n);
		return n*ins.employees().efficiency();
	}
	
	public default double industryFormatProductionRateEmpl(GText text, IndustryResource i, RoomInstance ins) {
		double n = IndustryUtil.calcProductionRate(i.rate, ((ROOM_PRODUCER) ins).industry(), ins);
		GFORMAT.fRel(text, n*ins.employees().efficiency(), n);
		return n*ins.employees().efficiency();
	}
	
	public default void industryHoverProductionRate(GBox b, IndustryResource i, RoomInstance ins) {
		IndustryUtil.hoverProductionRate(b, i.rate, ((ROOM_PRODUCER) ins).industry(), ins);
	}
	
	public default boolean industryIgnoreUI() {
		return false;
	}
}
