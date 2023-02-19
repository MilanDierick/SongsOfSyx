package init.biomes;

import init.paths.PATHS;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.tilemap.TBuilding;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.keymap.RCollection;

public class BUILDING_PREFS {

	private static BUILDING_PREFS self;

	public static void init() {
		self = new BUILDING_PREFS();
	}
	
	private final BUILDING_PREF MOUNTAIN;
	private final BUILDING_PREF OUTDOORS;
	private final LIST<BUILDING_PREF> BUILDING;
	private final LIST<BUILDING_PREF> ALL;
	private final RCollection<BUILDING_PREF> map;
	
	private BUILDING_PREFS() {
		LinkedList<BUILDING_PREF> all = new LinkedList<>();
		MOUNTAIN = new BUILDING_PREF("_MOUNTAIN", all) {
			@Override
			public SPRITE icon() {
				return SETT.TERRAIN().MOUNTAIN.getIcon();
			};
		};
		OUTDOORS = new BUILDING_PREF("_OUTDOORS", all) {
			@Override
			public SPRITE icon() {
				return SPRITES.icons().l.season_summer;
			};
		};
		String[] keys = PATHS.INIT_SETTLEMENT().getFolder("structure").getFiles();
		ArrayList<BUILDING_PREF> buildings = new ArrayList<BUILDING_PREF>(keys.length);
		int in = 0;
		for (String k : keys) {
			final int ind = in++;
			buildings.add(new BUILDING_PREF(k, all) {
				
				@Override
				public SPRITE icon() {
					return SETT.TERRAIN().BUILDINGS.getAt(ind).iconCombo;
				};
				
			});
		}
		this.BUILDING = buildings;
		this.ALL = new ArrayList<BUILDING_PREF>(all);
		
		map = new RCollection<BUILDING_PREF>("STRUCTURE") {
			
			{
				for (BUILDING_PREF p : ALL)
					map.put(p.key, p);
				map.expand();
			}
			
			@Override
			public BUILDING_PREF getAt(int index) {
				return ALL.get(index);
			}

			@Override
			public LIST<BUILDING_PREF> all() {
				return ALL;
			}
		};
		
		
	}
	
	public static BUILDING_PREF get(int tx, int ty) {
		if (SETT.TERRAIN().MOUNTAIN.isMountain(tx, ty))
			return self.MOUNTAIN;
		if (SETT.TERRAIN().get(tx, ty) instanceof TBuilding.BuildingComponent) {
			return self.BUILDING.get(((TBuilding.BuildingComponent)SETT.TERRAIN().get(tx, ty)).building().index());
		}
		return self.OUTDOORS;
	}
	
	public static BUILDING_PREF get(TBuilding building) {
		return self.BUILDING.get(building.index());
	}
	
	public static RCollection<BUILDING_PREF> MAP(){
		return self.map;
	}
	
	public static LIST<BUILDING_PREF> ALL(){
		return self.ALL;
	}
	
}
