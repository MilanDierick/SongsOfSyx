package init.resources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;

import init.D;
import init.biomes.TERRAIN;
import init.biomes.TERRAINS;
import init.paths.PATH;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.text.Str;
import util.keymap.KEY_COLLECTION;
import util.keymap.RCollection;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public final class Minable implements INDEXED{

	private static CharSequence ¤¤minable = "¤{0} Deposits";
	static {
		D.t(Minable.class);
	}
	public final RESOURCE resource;
	public final CharSequence name;
	public final TILE_SHEET sheet;
	public final double occurance;
	public final boolean onEverymap;
	public final COLOR tint;
	public final COLOR miniColor;
	public final int index;
	private final double[] terrainPref;
	public double fertilityIncrease;
	
	Minable(int index, TILE_SHEET sheet, Json json){
		occurance = 0.5;
		onEverymap = json.bool("ON_EVERY_MAP");
		tint = new ColorImp(json);
		miniColor = new ColorImp(json, "MINIMAP_COLOR");
		fertilityIncrease = json.d("FERTILITY_INCREASE", -1, 1);
		this.sheet = sheet;
		this.index = index;
		this.resource = RESOURCES.map().get(json);
		name = new Str(¤¤minable).insert(0, resource.name).trim();
		terrainPref = KEY_COLLECTION.fill(TERRAINS.MAP(), json, 1.0);
	}
	
	static RCollection<Minable> make(PATH pathData, PATH pathSprites) throws IOException{
		String folder = "minable";
		
		final PATH pd = pathData.getFolder(folder);
		final PATH ps = pathSprites.getFolder(folder);
		final HashMap<String, TILE_SHEET> spriteMap = new HashMap<>();
		
		Util util = new Util();
		String[] files = pd.getFiles(1,31);
		final ArrayList<Minable> res = new ArrayList<>(files.length);
		
		return new RCollection<Minable>("MINABLE") {
			{
				for (String p : files) {
					Json j = new Json(pd.get(p));
					String sprite = j.value("SPRITE");
					if (!spriteMap.containsKey(sprite)) {
						if (!ps.exists(sprite)) {
							
							String er = "Could not find texture file named: " + sprite + " Found only: " + System.lineSeparator();
							for (Entry<String, TILE_SHEET> e: spriteMap.entrySet()) {
								er += System.lineSeparator() + e.getKey();
							}
							j.error(er, sprite);
						}
						spriteMap.put(sprite, util.sprite(ps.get(sprite)));
						
					}
					Minable g = new Minable(res.size(), spriteMap.get(sprite), j);
					res.add(g);
					map.put(p, g);
				}
			}

			@Override
			public Minable getAt(int index) {
				return res.get(index);
			}

			@Override
			public LIST<Minable> all() {
				return res;
			}
		};
		
	}
	
	static final class Util {
		
		
		Util(){
			
		}
		
		private TILE_SHEET sprite(Path path) throws IOException {
			return new ITileSheet(path, 364, 94) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 8, 4, d.s16);
					s.singles.paste(true);
					return d.s16.saveGame();
				}
			}.get();
		}
		
	}

	@Override
	public int index() {
		return index;
	}
	
	public double terrain(TERRAIN t) {
		return terrainPref[t.index()];
	}
	
}
