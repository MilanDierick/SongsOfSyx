package settlement.room.main.category;

import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import settlement.room.main.ROOMS;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sets.*;

public final class RoomCategories {
	
	static CharSequence ¤¤other = "¤Other";
	
	{
		D.t(this);
	}
	
	public RoomCategories(ROOMS r) {
		// TODO Auto-generated constructor stub
	}
	
	private final ArrayListGrower<RoomCategorySub> all = new ArrayListGrower<>();
	
	public final RoomCategorySub MINES = new RoomCategorySub(all, D.g("Mines"), SPRITES.icons().m.raw_materials, new ColorImp(241/2, 94/2, 0));
	public final RoomCategorySub REFINERS = new RoomCategorySub(all, D.g("Refining"), SPRITES.icons().m.refiner,  new ColorImp(226/2, 195/2, 38/2));
	public final RoomCategorySub CRAFTING = new RoomCategorySub(all, D.g("Crafting"), SPRITES.icons().m.workshop,  new ColorImp(255/2, 155/2, 38/2));
	public final RoomCategorySub LAW = new RoomCategorySub(all, D.g("Law"), SPRITES.icons().m.law,  new ColorImp(180/2, 180/2, 180/2));
	public final RoomCategorySub FARMS = new RoomCategorySub(all, D.g("Farms"), SPRITES.icons().m.agriculture, new ColorImp(74/2, 119/2, 14/2));
	public final RoomCategorySub FISH = new RoomCategorySub(all, D.g("Aquaculture"), SPRITES.icons().m.fish, new ColorImp(74/2, 119/2, 14/2));
	public final RoomCategorySub HUSBANDRY = new RoomCategorySub(all, D.g("Husbandry"), SPRITES.icons().m.pasture, new ColorImp(74/2, 119/2, 14/2));
//	public final RoomCategorySub FOOD = new RoomCategorySub(D.g("Food"), SPRITES.icons().m.cat_food, new ColorImp(198/2, 106/2, 0));
	public final RoomCategorySub MILITARY = new RoomCategorySub(all, D.g("Military"), SPRITES.icons().m.shield_big, new ColorImp(127, 0, 0));
	public final RoomCategorySub ADMIN = new RoomCategorySub(all, D.g("Administration"), SPRITES.icons().m.building, new ColorImp(0, 127, 127));
//	public final RoomCategorySub HEALTH = new RoomCategorySub(all, D.g("Health"), SPRITES.icons().m.heart, new ColorImp(70/2, 0, 127));
//	public final RoomCategorySub TEMPLE = new RoomCategorySub(all, D.g("Religion"), SPRITES.icons().m.religion, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub BREEDING = new RoomCategorySub(all, D.g("Procreation"), SPRITES.icons().m.descrimination, new ColorImp(70/2, 0, 127));
//	public final RoomCategorySub ENTERTAINMENT = new RoomCategorySub(all, D.g("Entertainment"), SPRITES.icons().m.entertainment, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub DECOR = new RoomCategorySub(all, D.g("Decorations"), SPRITES.icons().m.infra, new ColorImp(70/2, 0, 127));
//	public final RoomCategorySub HOUSING = new RoomCategorySub(all, DicMisc.¤¤Housing, SPRITES.icons().m.building, new ColorImp(127, 0, 0));
	public final RoomCategorySub LOGISTICS = new RoomCategorySub(all, D.g("Logistics"), SPRITES.icons().m.urn, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub WATER = new RoomCategorySub(all, D.g("Water"), null, new ColorImp(70/2, 0, 127));
	
	public final RoomCategorySub DUMP = new RoomCategorySub(all, "", SPRITES.icons().m.urn, new ColorImp(70/2, 0, 127));
	
	public final RoomCategorySub SER_REL = new RoomCategorySub(all, D.g("Religion"), SPRITES.icons().m.religion, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub SER_CONSUMPTION = new RoomCategorySub(all, D.g("Distribution"), SPRITES.icons().m.cat_food, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub SER_HEALTH = new RoomCategorySub(all,  D.g("Health"), SPRITES.icons().m.heart, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub SER_ENTERTAIN = new RoomCategorySub(all, D.g("Entertainment"), SPRITES.icons().m.entertainment, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub SER_DEATH = new RoomCategorySub(all, D.g("Afterlife"), SPRITES.icons().m.skull, new ColorImp(70/2, 0, 127));
	public final RoomCategorySub SER_OTHER = new RoomCategorySub(all, D.g("Other"), SPRITES.icons().m.building, new ColorImp(70/2, 0, 127));
	
	public final LIST<RoomCategorySub> ALL = all;
	

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
			SER_REL,
			SER_CONSUMPTION,
			SER_HEALTH,
			SER_ENTERTAIN,
			SER_DEATH,
			SER_OTHER

			));

	
	public final RoomCategoryMain MAIN_INFRA = new RoomCategoryMain(D.g("Infrastructure"), SPRITES.icons().m.infra, new ArrayList<>(
			ADMIN,
			LAW,
			MILITARY,
			BREEDING,
			LOGISTICS,
			WATER
			));
	
	public final RoomCategoryMain MAIN_MISC = new RoomCategoryMain(D.g("Fundament"), SPRITES.icons().m.building, new ArrayList<>(
			DECOR
			));
	
	public final ArrayList<RoomCategoryMain> MAINS = new ArrayList<>(
			MAIN_AGRIULTURE,
			MAIN_INDUSTRY,
			MAIN_SERVICE,
			MAIN_INFRA,
			MAIN_MISC
			);
	
	public class RoomCategoryMain {
		
		public final CharSequence name;
		public final Icon icon;
		public final RoomCategorySub misc;
		public final LIST<RoomCategorySub> subs;
		private LIST<RoomBlueprintImp> all;
		

		
		RoomCategoryMain(CharSequence name, Icon icon, LIST<RoomCategorySub> subs) {
			this.name = name;
			this.icon = icon;
			this.subs = subs;
			misc = new RoomCategorySub(RoomCategories.this.all, ¤¤other, SPRITES.icons().m.questionmark, COLOR.WHITE100);

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
