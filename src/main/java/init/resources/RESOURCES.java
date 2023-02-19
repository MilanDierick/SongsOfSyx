package init.resources;

import java.io.IOException;
import java.util.Set;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sprite.TILE_SHEET;
import util.keymap.KEY_COLLECTION;
import util.keymap.RCollection;

public final class RESOURCES {
	
	private static Data data;
	
	public static final String KEY = "RESOURCE";
	public static final String KEYS = "RESOURCES";
	
	private static final class Data implements KEY_COLLECTION<RESOURCE>{
		
		private final LIST<RESOURCE> all;
		private final KeyMap<RESOURCE> map = new KeyMap<>();
		private final RCollection<Minable> minable;
		private final ResGroup drinks;
		private final RCollection<Growable> growable;
		private final Edibles edibles;
		private final RESOURCE STONE,WOOD,LIFESTOCK,ALCOHOL;
		private final int catAmount;
		private final ArmySupplies supplies;

		Data() throws IOException{
			data = this;
			
			PATH gInit = PATHS.INIT().getFolder("resource");
			PATH gText = PATHS.TEXT().getFolder("resource");
			PATH gSprite = PATHS.SPRITE().getFolder("resource");
			PATH gDebris = gSprite.getFolder("debris");
			
			String[] files;
			
			{
				String[] fixed = new String[] {
					"_STONE",
					"_WOOD",
					"_LIVESTOCK",
					"_ALCOHOL",
				};
				String[] mod = gInit.getFiles();
				files = new String[fixed.length+mod.length];
				for (int i = 0; i < fixed.length; i++)
					files[i] = fixed[i];
				for (int i = 0; i < mod.length; i++) {
					files[i+fixed.length] = mod[i];
				}
				
				final String[][] resources = new String[10][64];
				int[] catI = new int[10];
				boolean[] categories = new boolean[10];
				int cats = 0;
				
				for (String s : files) {
					Json in = new Json(gInit.get(s));
					int c = in.i("CATEGORY_DEFAULT", 0, 10);
					resources[c][catI[c]] = s;
					catI[c]++;
					if (!categories[c]) {
						cats++;
						categories[c] = true;
					}
				}
				
				catAmount = cats;

				int q = 0;
				for (int i = 0; i < resources.length; i++) {
					for (int k = 0; k < catI[i]; k++) {
						files[q++] = resources[i][k];
					}
				}
				
			}
			
			
			{
				
				
				ArrayList<RESOURCE> all = new ArrayList<>(64); 
				KeyMap<Sprite> spriteMap = new KeyMap<>();
				KeyMap<TILE_SHEET> debrisMap = new KeyMap<>();
				
				
				

				
				for (String key : files) {
					new RESOURCE(all, key, gInit, gText, gSprite, gDebris, map, spriteMap, debrisMap);
				}
				
				
				this.all = new ArrayList<RESOURCE>(all);
				map.expand();
				
				for (RESOURCE r : RESOURCES.ALL()) {
					Json j = new Json(gInit.get(r.key));
					if (j.has("VALUE_SIMIILAR_TO")) {
						String[] vv = j.values("VALUE_SIMIILAR_TO");
						for (String s : vv) {
							r.tradeSameAs = r.tradeSameAs.join(get(s, j));
							
						}
					}
				}
				
				
				
				STONE = map.get("_STONE");
				WOOD = map.get("_WOOD");
				LIFESTOCK = map.get("_LIVESTOCK");
				ALCOHOL = map.get("_ALCOHOL");
				
				//map.debug();
			}
			
			{
				minable = Minable.make(gInit, gSprite);
				growable = Growable.make(gInit, gSprite);
			}
			
			edibles = new Edibles(all);
			drinks = new ResGroup("DRINKABLE", ALCOHOL);
			supplies = new ArmySupplies();
		}



		@Override
		public RESOURCE tryGet(String value) {
			return map.get(value);
		}



		@Override
		public String key() {
			return KEY;
		}



		@Override
		public LIST<RESOURCE> all() {
			return all;
		}



		@Override
		public Set<String> available() {
			return map.keys();
		}
		
	}
	
	private RESOURCES() {
		
	}
	

	
	public static void init() throws IOException {
		
		new Data();
		
	}
	
	public static LIST<RESOURCE> ALL(){
		return data.all;
	}
	
	public static RCollection<Minable> minables(){
		return data.minable;
	}
	
	public static RCollection<Growable> growable(){
		return data.growable;
	}
	
	public static ResGroup DRINKS() {
		return data.drinks;
	}
	
	public static Edibles EDI() {
		return data.edibles;
	}
	
	public static RESOURCE STONE() {
		return data.STONE;
	}
	
	public static RESOURCE WOOD() {
		return data.WOOD;
	}
	
	public static RESOURCE LIVESTOCK() {
		return data.LIFESTOCK;
	}
	
	public static RESOURCE ALCOHOL() {
		return data.ALCOHOL;
	}
	
	public static int CATEGORIES() {
		return data.catAmount;
	}
	
	public static ArmySupplies SUP() {
		return data.supplies;
	}
	
	public static KEY_COLLECTION<RESOURCE> map(){
		return data;
	}
}
