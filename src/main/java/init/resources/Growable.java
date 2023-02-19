package init.resources;

import java.io.IOException;

import init.biomes.CLIMATE;
import init.biomes.CLIMATES;
import init.paths.PATH;
import settlement.main.RenderData;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.keymap.RCollection;
import util.rendering.ShadowBatch;

public final class Growable implements INDEXED{

	public final RESOURCE resource;
	public final double seasonalOffset;
	public final double growthValue;
	public final COLOR colorMinimap;
	public final int index;
	private final double[] climate;
	public final GrowableSprite sprite;
	
	private Growable(int index, Json json) throws IOException{
		seasonalOffset = json.d("SEASONAL_OFFSET", 0, 1); 
		growthValue = json.d("GROWTH_VALUE", 0, 1.0);
		this.colorMinimap = new ColorImp(json, "MINIMAP_COLOR");
		this.index = index;
		this.resource = RESOURCES.map().get(json);
		climate = new double[CLIMATES.ALL().size()];
		CLIMATES.MAP().fill("CLIMATE_BONUS", climate, json, 0, 10000);
		
		{
			json = json.json("SPRITE");
			
			
			double poll = json.d("POLLEN", 0, 10);
			double wind = json.dTry("WIND_SWAY", 0, 10, 1);
			
			sprite = new GrowableSprite(json.value("SPRITE"), wind, poll);
			
			sprite.setPollenColor(new ColorImp(json, "COLOR_POLLEN"));
			
			set(json.json("STEM"), sprite.trunk);
			set(json.json("GROWTH"), sprite.growth);
			
			
		}
	}
	
	private static void set(Json json, GrowableSprite.Part part) {
		part.sheightoverGround = json.d("SHADOW_HEIGHT", 0, 32);
		part.sheight = json.d("SHADOW_LENGTH", 0, 32);
		part.setColors(new ColorImp(json, "DEAD"), new ColorImp(json, "LIVE"), new ColorImp(json, "RIPE"));
		if (json.has("WIND_SWAY"))
			part.sway = json.d("WIND_SWAY", 0, 10);
	}
	
	static RCollection<Growable> make(final PATH pathData, final PATH pathSprites) throws IOException{
		String folder = "growable";
		final PATH pd = pathData.getFolder(folder);
		
		
		
		String[] files = pd.getFiles();
		final ArrayList<Growable> res = new ArrayList<>(files.length);
		
		return new RCollection<Growable>("GROWABLE") {
			{
				for (String p : files) {
					Json j = new Json(pd.get(p));
					Growable g = new Growable(res.size(), j);
					res.add(g);
					map.put(p, g);
				}
			}

			@Override
			public Growable getAt(int index) {
				return res.get(index);
			}

			@Override
			public LIST<Growable> all() {
				return res;
			}
		};
		
	}
	

	public void render(SPRITE_RENDERER r, ShadowBatch shadowBatch, RenderData.RenderIterator it, int amount, boolean ripe) {
		
	}
	
	@Override
	public int index() {
		return index;
	}
	
	public double availability(CLIMATE c) {
		return climate[c.index()];
	}

	
}
