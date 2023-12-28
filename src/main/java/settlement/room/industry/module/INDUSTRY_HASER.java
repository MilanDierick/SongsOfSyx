package settlement.room.industry.module;

import game.boosting.Boostable;
import game.values.GVALUES;
import init.resources.RESOURCE;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import world.regions.Region;

public interface INDUSTRY_HASER {

	public LIST<Industry> industries();

	static LIST<Industry> createIndustries(RoomBlueprintImp blue, RoomInitData init, RoomBoost[] boosts,
			Boostable bonus) {

		DOUBLE_O<Region> rr = new DOUBLE_O<Region>() {

			@Override
			public double getD(Region t) {
				return 1.0;
			}
			
		};
		return createIndustries(blue, init, boosts, bonus, rr);
	}
	
	static LIST<Industry> createIndustries(RoomBlueprintImp blue, RoomInitData init, RoomBoost[] boosts,
			Boostable bonus, DOUBLE_O<Region> regBonus) {

		Json[] js = init.data().jsons("INDUSTRIES", 1);

		ArrayList<Industry> res = new ArrayList<>(js.length);
		for (Json j : js) {
			Industry i = new Industry(blue, j, boosts, bonus) {
				@Override
				public double getRegionBonus(Region reg) {
					return regBonus.getD(reg);
				}
			};
			if (i.outs().size() == 0)
				j.error(blue.info + " has no out resources declared. This can be due to an outdated mod.", "INDUSTRIES");
			res.add(i);
		}
		if (res.size() > 1) {
			int ii = 0;
			for (int i = 0; i < res.size(); i++) {
				RESOURCE u =  unique(res.get(i), res);
				SPRITE icon = blue.iconBig().twin(u.icon().scaled(0.5), DIR.SE, 1);
				String desc = "";
				for (IndustryResource ir : res.get(i).ins()) {
					if (desc.length() > 0)
						desc += " + ";
					desc += ir.resource.name;
				}
				desc += " -> " + res.get(i).outs().get(0).resource.name;
				res.get(i).lockable = GVALUES.FACTION.LOCK.push("ROOM_" + blue.key + "_RECIPE_" + ii, blue.info.name + ": " + unique(res.get(i), res).name, desc, icon);
				ii++;
				
			}
			
			
		}
		
		return res;

	}
	
	
	
	static RESOURCE unique(Industry ins, LIST<Industry> others) {
		RESOURCE res = ins.outs().get(0).resource;
		boolean unique = true;
		for (Industry i : others) {
			if (i != ins && i.outs().get(0).resource == res)
				unique = false;
		}
		if (unique)
			return res;
		for (IndustryResource r : ins.ins()) {
			unique = true;
			for (Industry i : others) {
				if (i != ins) {
					for (IndustryResource or : i.ins()) {
						if (or.resource == r.resource) {
							unique = false;
						}
					}
				}
			}
			if (unique)
				return r.resource;
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
