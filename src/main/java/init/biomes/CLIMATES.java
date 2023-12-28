package init.biomes;


import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import game.boosting.*;
import game.faction.Faction;
import game.faction.npc.NPCBonus;
import game.faction.npc.ruler.Royalty;
import game.values.GVALUES;
import init.D;
import init.paths.PATHS;
import init.race.POP_CL;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.*;
import util.data.BOOLEANO;
import util.info.INFO;
import util.keymap.KEY_COLLECTION;
import world.WORLD;
import world.regions.Region;

public final class CLIMATES {

	public final static String KEY = "CLIMATE";
	private static Data d;
	
	private CLIMATES() {
		
	}
	
	public static void init() throws IOException {
		new Data();
	}
	
	
	public static CLIMATE COLD() {
		return d.COLD;
	}
	
	public static CLIMATE TEMP() {
		return d.TEMPERATE;
	}
	
	public static CLIMATE HOT() {
		return d.HOT;
	}
	
	public static LIST<CLIMATE> ALL(){
		return d.all();
	}
	
	public static KEY_COLLECTION<CLIMATE> MAP(){
		return d;
	}
	
	public static INFO INFO() {
		return d.info;
	}
	
	public static BoostSpecs BONUS() {
		return d.boosters;
	}
	
	public static void pushBonuses(Json json, Boostable bo) {
		if (!json.has(KEY))
			return;
		double vv[] = new double[ALL().size()];
		Arrays.fill(vv, 1.0);
		CLIMATES.MAP().fill(vv, json, 0, 2000);
		for (CLIMATE c : CLIMATES.ALL()) {
			if (vv[c.index()] == 1.0)
				continue;
			c.boosters.pushPromise(bo, null, vv[c.index()], true);
		}
		
		
	}

	public static BoostSpec pushIfDoesntExist(CLIMATE c, double v, Boostable bo, boolean isMul) {
		String k = bo.key + isMul;
		double none = isMul ? 1 : 0;
		if (d.bvmap.containsKey(k) && d.bvmap.get(k).values[c.index()] == none)
			return null;
		
		if (!d.bvmap.containsKey(k)) {
			d.bvmap.put(k, new BV(d.boosters, bo, isMul));
		}
		
		BV bv = d.bvmap.get(k);
		bv.set(c, v);
		c.boosters.push(bo, v, isMul);
		return bv.spec;
	}
	
	
	private static final class Data implements KEY_COLLECTION<CLIMATE>{

		private final CLIMATE COLD;
		private final CLIMATE TEMPERATE;
		private final CLIMATE HOT;
		private final ArrayList<CLIMATE> all = new ArrayList<>(3);
		private final KeyMap<CLIMATE> map = new KeyMap<CLIMATE>();
		private final INFO info;
		private final BoostSpecs boosters;
		private final KeyMap<BV> bvmap = new KeyMap<BV>();
		
		Data() throws IOException{
			D.gInit(CLIMATES.class);
			info = new INFO(D.g("Climate"), D.g("desc", "Climate zones have a range of bonuses and drawbacks. They also have different base temperatures, which can lead to exposure and death for your subjects depending on their natural resilience to hot and cold."));
			d = this;
			Json j = new Json(PATHS.CONFIG().get(KEY));
			COLD = new CLIMATE(
					all, "COLD", 
					D.g("Cold"),
					D.g("cold_desc", "Very cold winters. Unique crops. Low disease rates."),
					j);
			TEMPERATE = new CLIMATE(
					all, "TEMPERATE", 
					D.g("Temperate"),
					D.g("temp_desc", "Varying temperature."),
					j);
			HOT = new CLIMATE(
					all, "HOT", 
					D.g("Warm"),
					D.g("warm_desc", "Hot summers."),
					j);
			
			for (CLIMATE c : all)
				map.put(c.key, c);
			
			boosters = new BoostSpecs(info.name, UI.icons().s.heat, true);
			
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					
					for (CLIMATE c : CLIMATES.ALL()) {
						
						for (BoostSpec s : c.boosters.all()) {
							String k = s.boostable.key + s.booster.isMul;
							if (!bvmap.containsKey(k)) {
								bvmap.put(k, new BV(boosters, s.boostable, s.booster.isMul));
							}
							bvmap.get(k).set(c, s.booster.to());
						}
					}
					
				}
			};
			BOOSTING.connecter(a);
			
			for (CLIMATE c : all) {
				GVALUES.FACTION.push("CLIMATE_" + c.key, info.name + ": " + c.name, new BOOLEANO<Faction>() {
					
					@Override
					public boolean is(Faction t) {
						if (t.capitolRegion() != null)
							return WORLD.CLIMATE().getter.get(t.capitolRegion().cx(), t.capitolRegion().cy()) == c;
						return false;
					}
				});
				
			}
			
		}
		

		
		
		@Override
		public CLIMATE tryGet(String value) {
			return map.get(value);
		}

		@Override
		public String key() {
			return KEY;
		}

		@Override
		public LIST<CLIMATE> all() {
			return all;
		}
		
		@Override
		public Set<String> available() {
			return map.keys();
		}
		
	}
	
	private static class BV extends Booster{

		private double from;
		private double to;
		private final double[] values = new double[CLIMATES.ALL().size()];
		private final boolean isMul;
		public final BoostSpec spec;
		
		BV(BoostSpecs bos, Boostable target, boolean isMul){
			super(new BSourceInfo(CLIMATES.INFO().name, UI.icons().s.heat), isMul);
			this.isMul = isMul;
			if (isMul)
				Arrays.fill(values, 1.0);
			set();
			spec = bos.push(this, target);
		}
		
		void set(CLIMATE c, double value) {
			values[c.index()] = value;
			set();
		}
		
		private void set() {
			if (isMul) {
				from = 1.0;
				to = 1.0;
			}else {
				from = 0;
				to = 0;
			}
			
			for (double v : values) {
				
				from = Math.min(v, from);
				to = Math.max(v, to);
			}
		}
		
		@Override
		public double get(Boostable bo, BOOSTABLE_O o) {
			return o.boostableValue(bo, this);
		}
		
		@Override
		public double vGet(Region reg) {
			double res = 0;
			for (int ci = 0; ci < CLIMATES.ALL().size(); ci++) {
				res += values[ci]*reg.info.climate(CLIMATES.ALL().get(ci));
			}
			return res;
		}
		
		@Override
		public double vGet(NPCBonus f) {
			return vGet(f.faction.capitolRegion());
		}

		@Override
		public double vGet(Induvidual indu) {
			return values[SETT.WORLD_AREA().climate().index()];
		}

		@Override
		public double vGet(Div div) {
			return values[SETT.WORLD_AREA().climate().index()];
		}

		@Override
		public double vGet(Faction f) {
			return values[WORLD.CLIMATE().getter.get(f.capitolRegion().cx(), f.capitolRegion().cy()).index()];
		}

		@Override
		public double vGet(POP_CL reg) {
			return values[SETT.WORLD_AREA().climate().index()];
		}

		@Override
		public double vGet(Royalty roy) {
			return values[WORLD.CLIMATE().getter.get(roy.court.faction.capitolRegion().cx(), roy.court.faction.capitolRegion().cy()).index()];
		}

		@Override
		public boolean has(Class<? extends BOOSTABLE_O> b) {
			return b != Race.class;
		}

		@Override
		public double from() {
			return from;
		}

		@Override
		public double to() {
			return to;
		}

		@Override
		public double vGet(Race race) {
			return 0;
		}
		
		
	}
	
	
	
}
