package world.map.buildings.camp;

import java.io.IOException;

import game.faction.FACTIONS;
import game.statistics.GRequirementWrapper;
import game.statistics.G_REQ;
import init.biomes.CLIMATES;
import init.biomes.TERRAINS;
import init.paths.PATH;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sprite.TILE_SHEET;
import util.keymap.KEY_COLLECTION;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;
import world.World;

final class WCampType implements INDEXED{

	private final int index;
	public final TILE_SHEET sheet;
	public final COLOR cMask;
	public final LIST<CharSequence> names;
	public final Race race;
	public final int min;
	public final int max;
	public final double reMin;
	public final double reMax;
	public final double[] climates;
	public final double[] terrains;
	private final LIST<G_REQ> requirements;
	public final LIST<G_REQ> rmin;
	
	WCampType(LISTE<WCampType> all, Json jdata, Json jtext, TILE_SHEET sheet) throws IOException{
		index = all.add(this);
		this.sheet = sheet;
		cMask = new ColorImp(jdata, "COLOR_MASK");
		names = new ArrayList<>(jtext.texts("NAMES", 1, 500));
		race = RACES.map().get(jdata.value("RACE"), jdata);
		Data min = new Data(jdata.json("FROM"));
		Data max = new Data(jdata.json("TO"));
		rmin = min.req;
		climates = KEY_COLLECTION.fill(CLIMATES.MAP(), jdata, 1);
		terrains = KEY_COLLECTION.fill(TERRAINS.MAP(), jdata, 100);
		
		if (!checkReq(min, max)) {
			jdata.error("Requirements must match. Same requirements must be listed in max and min, and in the same order", "TO");
		}
		
		this.min = min.amount;
		this.max = max.amount;
		this.reMin = min.replenishRateDay;
		this.reMax = max.replenishRateDay;
		
		ArrayList<G_REQ> reqs = new ArrayList<>(min.req.size());
		for (int i = 0; i < min.req.size(); i++)
			reqs.add(new Req(min.req.get(i), max.req.get(i)));
		this.requirements = reqs;
	}
	
	private static boolean checkReq(Data min, Data max) {
		if (min.req.size() != max.req.size()) {
			return false;
		}
		
		for (int i = 0; i < min.req.size(); i++) {
			if (!min.req.get(i).isSameBase(max.req.get(i))) {
				
				return false;
			}
		}
		return true;
	}
	
	static LIST<WCampType> types() throws IOException{
		
		LinkedList<WCampType> all = new LinkedList<>();
		KeyMap<TILE_SHEET> sheets = new KeyMap<>();
		
		PATH pi = PATHS.INIT_WORLD().getFolder("camps");
		PATH pt = PATHS.TEXT_WORLD().getFolder("camps");
		PATH ps = PATHS.SPRITE_WORLD().getFolder("camps");
		
		for (String file : pi.getFiles()) {
			
			Json jdata = new Json(pi.get(file));
			Json jtext = new Json(pt.get(file));
			
			String ssprite = jdata.value("SPRITE");
			TILE_SHEET sheet = sheets.get(ssprite);
			if (sheet == null) {
				TILE_SHEET s = new ITileSheet(ps.get(ssprite), 132, 126) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.singles.init(0, 0, 1, 1, 2, 4, d.s24);
						s.singles.paste(3, true);
						return d.s24.saveGame();
					}
				}.get();
				sheets.put(ssprite, s);
				sheet = s;
			}
			new WCampType(all, jdata, jtext, sheet);
		}
		
		return new ArrayList<>(all);
		
	}
	
	@Override
	public int index() {
		return index;
	}
	
	static class Data {
		
		public final LIST<G_REQ> req;
		public final int amount;
		public final double replenishRateDay;
		
		Data(Json j){
			amount = j.i("IMMIGRANTS", 1, 10000);
			replenishRateDay = j.d("REPLENISH_PER_DAY", 0, 1000);
			req = G_REQ.READ(j);
		}
		
	}
	
	public LIST<G_REQ> requiremets(double size){
		tmpSize = size;
		return requirements;
	}
	
	private double tmpSize = 0;
	
	private class Req extends GRequirementWrapper {

		public final int from;
		public final int to;
		
		public Req(G_REQ min, G_REQ max) {
			super(min);
			this.from = min.target();
			this.to = max.target();
		}

		@Override
		public int target() {
			
			double current = World.camps().factions.current(FACTIONS.player(), WCampType.this);
			double total = World.camps().factions.total(WCampType.this);
			
			double d = (current+tmpSize*max)/total;
			
			int a = (int) Math.ceil(from+(to-from)*d);
			
			return a;
		}
		
		
	}
	
}
