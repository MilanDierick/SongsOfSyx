package init.tech;

import java.util.LinkedList;

import game.GAME;
import init.boostable.*;
import init.sprite.SPRITES;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomsJson;
import settlement.tilemap.Floors.Floor;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.info.GFORMAT;

public class Unlocks implements BOOSTER {

	protected final LIST<BBoost> bonuses;
	protected final LIST<RoomBlueprintImp> roomUnlocks;
	protected final LIST<Industry> industryUnlocks;
	protected final LIST<RoomBlueprintImp> upgrades;
	private final LIST<Floor> roads;
	protected final int nobles;
	private final CharSequence name;
	
	public Unlocks(CharSequence name, Json data) {
		
		this.name = name;
		bonuses = BOOSTABLES.boosts(data);
		
		
		roomUnlocks = RoomsJson.list(data);
		upgrades = RoomsJson.list("UPGRADES", data);
		roads = SETT.FLOOR().getManyWarn(data);
		
		LinkedList<Industry> res = new LinkedList<>();
		
		if (data.has("INDUSTRIES"))
			new RoomsJson("INDUSTRIES", data) {
				
				@Override
				public void doWithTheJson(RoomBlueprintImp r, Json j, String key) {
					if (!(r instanceof INDUSTRY_HASER)) {
						GAME.WarnLight(data.errorGet("No industry named " + r.key + " " + r.key, r.key));
					}else {
						INDUSTRY_HASER p = (INDUSTRY_HASER) r;
						int ii = j.i(key, 1, p.industries().size());
						res.add(p.industries().get(ii));
					}
					
				}
			};
		industryUnlocks = new ArrayList<>(res);
		
		nobles = data.i("NOBLES", 0, 10000, 0);
		
		
	}
	
//	public Unlocks(BOOSTABLE b, double boost) {
//		this.name = "";
//		bonuses = new ArrayList<BBoost>(new BBoost(b, boost));
//		roomUnlocks = new ArrayList<RoomBlueprintImp>(0);
//		industryUnlocks = new ArrayList<Industry>(0);
//		upgrades = new ArrayList<RoomBlueprintImp>(0);
//		roads = new ArrayList<Floor>(0);
//		nobles = 0;
//	}
	
	public LIST<RoomBlueprintImp> roomsUnlocks(){
		return roomUnlocks;
	}
	
	public LIST<Industry> unlocksIndustry(){
		return industryUnlocks;
	}
	
	public LIST<RoomBlueprintImp> unlocksUpgrades(){
		return upgrades;
	}
	
	public LIST<Floor> unlocksRoads(){
		return roads;
	}
	
	public void hoverInfoGet(GUI_BOX text) {
		GBox b = (GBox) text;
		
		if (nobles > 0) {
			b.add(b.text().lablifySub().add(HCLASS.NOBLE.names));
			b.add(GFORMAT.i(b.text(), nobles));
			b.NL();
		}
		
		BOOSTER.hover(b, bonuses);
		
		if (roomsUnlocks().size() > 0) {
			b.NL(8);
			b.add(b.text().lablify().add(DicMisc.¤¤Unlocks));
			b.NL();
			for (RoomBlueprintImp bb : roomsUnlocks()) {
				b.add(bb.iconBig().nomal);
				b.add(b.text().lablifySub().add(bb.info.names));
				b.NL();
			}
		}
		
		if (unlocksIndustry().size() > 0) {
			b.NL(8);
			b.add(b.text().lablify().add(DicMisc.¤¤Unlocks));
			b.NL();
			for (Industry bb : unlocksIndustry()) {
				b.add(bb.blue.iconBig().nomal);
				b.space();
				for (IndustryResource r : bb.ins())
					b.add(r.resource.icon());
				b.add(SPRITES.icons().m.arrow_right);
				for (IndustryResource r : bb.outs())
					b.add(r.resource.icon());
				b.NL();
			}
		}
		
		if (unlocksUpgrades().size() > 0) {
			b.NL(8);
			b.add(b.text().lablify().add(DicMisc.¤¤Unlocks));
			b.NL();
			for (RoomBlueprintImp bb : unlocksUpgrades()) {
				b.add(bb.iconBig().nomal);
				b.rewind(8);
				b.add(SPRITES.icons().m.arrow_up);
				b.space();
			}
			b.NL();
		}
		
		if (unlocksRoads().size() > 0) {
			b.NL(8);
			b.add(b.text().lablify().add(DicMisc.¤¤Unlocks));
			b.NL();
			for (Floor bb : unlocksRoads()) {
				b.add(bb.getIcon().nomal);
			}
			b.NL();
		}
		
	}

	@Override
	public CharSequence boosterName() {
		return name;
	}

	@Override
	public LIST<BBoost> boosts() {
		return  bonuses;
	}
	
}
