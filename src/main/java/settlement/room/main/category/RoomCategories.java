package settlement.room.main.category;

import init.D;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.room.main.ROOMS;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sets.*;
import util.dic.DicMisc;

public final class RoomCategories {
	
	static CharSequence ¤¤other = "¤Other";
	
	{
		D.t(this);
	}
	
	public RoomCategories(ROOMS r) {
		// TODO Auto-generated constructor stub
	}
	
	public final RoomCategorySub MINES = new RoomCategorySub(D.g("Mines"), SPRITES.icons().m.raw_materials, new ColorImp(241/2, 94/2, 0));
	public final RoomCategorySub REFINERS = new RoomCategorySub(D.g("Refining"), SPRITES.icons().m.refiner,  new ColorImp(226/2, 195/2, 38/2));
	public final RoomCategorySub CRAFTING = new RoomCategorySub(D.g("Crafting"), SPRITES.icons().m.workshop,  new ColorImp(255/2, 155/2, 38/2));
	public final RoomCategorySub LAW = new RoomCategorySub(D.g("Law"), SPRITES.icons().m.law,  new ColorImp(180/2, 180/2, 180/2));
	public final RoomCategorySub FARMS = new RoomCategorySub(D.g("Farms"), SPRITES.icons().m.agriculture, new ColorImp(74/2, 119/2, 14/2));
	public final RoomCategorySub FISH = new RoomCategorySub(D.g("Aquaculture"), SPRITES.icons().m.fish, new ColorImp(74/2, 119/2, 14/2));
	public final RoomCategorySub HUSBANDRY = new RoomCategorySub(D.g("Husbandry"), SPRITES.icons().m.pasture, new ColorImp(74/2, 119/2, 14/2));
//	public final RoomCategorySub FOOD = new RoomCategorySub(D.g("Food"), SPRITES.icons().m.cat_food, new ColorImp(198/2, 106/2, 0));
	public final RoomCategorySub MILITARY = new RoomCategorySub(D.g("Military"), SPRITES.icons().m.shield_big, new ColorImp(127, 0, 0));
	public final RoomCategorySub KNOWLEDGE = new RoomCategorySub(D.g("Knowledge"), SPRITES.icons().m.book, new ColorImp(0, 127, 127));
	public final RoomCategorySub HEALTH = new RoomCategorySub(D.g("Health"), SPRITES.icons().m.heart, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub TEMPLE = new RoomCategorySub(D.g("Religion"), SPRITES.icons().m.religion, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub BREEDING = new RoomCategorySub(D.g("Procreation"), SPRITES.icons().m.descrimination, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub ENTERTAINMENT = new RoomCategorySub(D.g("Entertainment"), SPRITES.icons().m.entertainment, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub DECOR = new RoomCategorySub(D.g("Decorations"), SPRITES.icons().m.infra, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub HOUSING = new RoomCategorySub(DicMisc.¤¤Housing, SPRITES.icons().m.building, new ColorImp(127, 0, 0));
	public final RoomCategorySub LOGISTICS = new RoomCategorySub(D.g("Logistics"), SPRITES.icons().m.urn, new ColorImp(70/2, 0, 127));
	
	public final RoomCategorySub DUMP = new RoomCategorySub("", SPRITES.icons().m.urn, new ColorImp(70/2, 0, 127));
	
	public final ArrayList<RoomCategorySub> ALL = new ArrayList<>(
			MINES,
			REFINERS,
			CRAFTING,
			LAW,
			FARMS,
			FISH,
			HUSBANDRY,
			MILITARY,
			KNOWLEDGE,
			HEALTH,
			TEMPLE,
			BREEDING,
			ENTERTAINMENT,
			DECOR,
			HOUSING,
			LOGISTICS
			
			
			);
	

	public final RoomCategoryMain MAIN_AGRIULTURE = new RoomCategoryMain(D.g("Agriculture"), SPRITES.icons().m.agriculture, new ArrayList<>(
			FARMS,
			HUSBANDRY,
			FISH
			));
	
	public final RoomCategoryMain MAIN_INDUSTRY = new RoomCategoryMain(D.g("Work"), SPRITES.icons().m.refiner, new ArrayList<>(
			MINES,
			REFINERS,
			CRAFTING
			));
	
	public final RoomCategoryMain MAIN_SERVICE = new RoomCategoryMain(D.g("Civics"), SPRITES.icons().m.service, new ArrayList<>(
			HEALTH,
			
			ENTERTAINMENT,
			TEMPLE,
			HOUSING
			));

	
	public final RoomCategoryMain MAIN_INFRA = new RoomCategoryMain(D.g("Infrastructure"), SPRITES.icons().m.infra, new ArrayList<>(
			KNOWLEDGE,
			LAW,
			MILITARY,
			BREEDING,
			LOGISTICS
			));
	
	public final RoomCategoryMain MAIN_MISC = new RoomCategoryMain(D.g("Fundament"), SPRITES.icons().m.building, new ArrayList<>(
			DECOR
			));
	
	public final ArrayList<RoomCategoryMain> MAINS = new ArrayList<>(
			MAIN_AGRIULTURE,
			MAIN_INDUSTRY,
			MAIN_SERVICE,
			MAIN_INFRA
			);
	
	public static class RoomCategoryMain {
		
		public final CharSequence name;
		public final ICON.MEDIUM icon;
		public final RoomCategorySub misc;
		public final LIST<RoomCategorySub> subs;
		private LIST<RoomBlueprintImp> all;
		

		
		RoomCategoryMain(CharSequence name, ICON.MEDIUM icon, LIST<RoomCategorySub> subs) {
			this.name = name;
			this.icon = icon;
			this.subs = subs;
			misc = new RoomCategorySub(¤¤other, SPRITES.icons().m.questionmark, COLOR.WHITE100);

		}
		
		private void n() {
			LinkedList<RoomBlueprintImp> all = new LinkedList<>();
			for (RoomCategorySub s : subs) {
				for (RoomBlueprintImp p : s.rooms())
					all.add(p);
			}
			for (RoomBlueprintImp p : misc.rooms())
				all.add(p);
			this.all = new ArrayList<>(all);
		}

		
		public LIST<RoomBlueprintImp> all(){
			if (all == null) {
				n();
			}
			return all;
		}

	}
	
}
